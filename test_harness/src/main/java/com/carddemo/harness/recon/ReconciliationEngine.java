package com.carddemo.harness.recon;

import com.carddemo.harness.codec.CardDemoLayouts;
import com.carddemo.harness.codec.FieldDef;
import com.carddemo.harness.loader.GoldenDataLoader;
import com.carddemo.harness.loader.RepoPaths;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ReconciliationEngine {
  private final RepoPaths paths;

  public ReconciliationEngine() {
    this(new RepoPaths());
  }

  public ReconciliationEngine(RepoPaths paths) {
    this.paths = paths;
  }

  public ReconReport reconcile(Map<String, List<Map<String, Object>>> records,
      Map<String, Object> manifest) throws IOException {
    Map<String, DatasetRecon> datasetResults = new LinkedHashMap<>();
    Map<String, Object> datasets = GoldenDataLoader.object(manifest.get("datasets"));
    for (Map.Entry<String, Object> entry : datasets.entrySet()) {
      String name = entry.getKey();
      Map<String, Object> metadata = GoldenDataLoader.object(entry.getValue());
      List<Map<String, Object>> datasetRecords = records.getOrDefault(name, List.of());
      datasetResults.put(name, calculateDataset(name, datasetRecords, metadata));
    }
    return new ReconReport(datasetResults, referentialIntegrity(records,
        (List<Map<String, Object>>) manifest.getOrDefault("referentialIntegrity", List.of())));
  }

  private DatasetRecon calculateDataset(String name, List<Map<String, Object>> records,
      Map<String, Object> metadata) throws IOException {
    List<String> keyFields = ((List<?>) metadata.get("keyFields")).stream().map(String::valueOf).toList();
    List<String> keys = records.stream().map(record -> keyValue(name, record, keyFields)).toList();
    Set<String> distinct = new LinkedHashSet<>(keys);
    FieldDef firstKey = keyFields.size() == 1
        ? findField(CardDemoLayouts.get(name).fields(), keyFields.get(0)) : null;
    Comparator<String> keyOrder = firstKey != null && !firstKey.pic().alphanumeric()
        && firstKey.usage() != com.carddemo.harness.codec.Usage.DISPLAY
        ? Comparator.comparing(BigInteger::new) : String::compareTo;
    String keyMin = keys.stream().min(keyOrder).orElse("");
    String keyMax = keys.stream().max(keyOrder).orElse("");
    int duplicates = keys.size() - distinct.size();
    Map<String, String> totals = controlTotals(name, records, keyFields);
    String sourceFile = String.valueOf(metadata.get("sourceFile"));
    return new DatasetRecon(records.size(), totals, keyMin, keyMax, duplicates == 0, duplicates,
        sha256(Files.readAllBytes(paths.resolve(sourceFile))));
  }

  private Map<String, String> controlTotals(String dataset, List<Map<String, Object>> records,
      List<String> keyFields) {
    List<FieldDef> numericFields = new ArrayList<>();
    collectNumericFields(CardDemoLayouts.get(dataset).fields(), numericFields);
    Map<String, String> totals = new LinkedHashMap<>();
    for (FieldDef field : numericFields) {
      if (keyFields.contains(field.name()) || (!field.pic().signed() && field.pic().scale() == 0)) {
        continue;
      }
      BigDecimal total = BigDecimal.ZERO;
      for (Map<String, Object> record : records) {
        Object value = getValue(record, field.name());
        if (value instanceof String text) {
          total = total.add(new BigDecimal(text));
        }
      }
      totals.put(field.name(), total.setScale(field.pic().scale()).toPlainString());
    }
    return totals;
  }

  private List<ReferentialCheck> referentialIntegrity(Map<String, List<Map<String, Object>>> records,
      List<Map<String, Object>> expectedChecks) {
    List<ReferentialCheck> result = new ArrayList<>();
    for (Map<String, Object> metadata : expectedChecks) {
      String name = String.valueOf(metadata.get("check"));
      Relationship relationship = relationship(name);
      List<Map<String, Object>> children = records.getOrDefault(relationship.childDataset, List.of());
      Set<String> parentKeys = records.getOrDefault(relationship.parentDataset, List.of()).stream()
          .map(record -> relationship.parentFields.stream()
              .map(field -> String.valueOf(record.get(field))).collect(Collectors.joining("|")))
          .collect(Collectors.toSet());
      List<String> orphans = children.stream()
          .map(record -> relationship.childFields.stream()
              .map(field -> String.valueOf(record.get(field))).collect(Collectors.joining("|")))
          .filter(key -> !parentKeys.contains(key)).toList();
      result.add(new ReferentialCheck(name, children.size(), orphans.size(),
          orphans.stream().distinct().sorted().toList()));
    }
    return result;
  }

  private Relationship relationship(String check) {
    return switch (check) {
      case "cardxref.XREF-ACCT-ID -> acctdata.ACCT-ID" ->
          rel("cardxref", "XREF-ACCT-ID", "acctdata", "ACCT-ID");
      case "cardxref.XREF-CUST-ID -> custdata.CUST-ID" ->
          rel("cardxref", "XREF-CUST-ID", "custdata", "CUST-ID");
      case "cardxref.XREF-CARD-NUM -> carddata.CARD-NUM" ->
          rel("cardxref", "XREF-CARD-NUM", "carddata", "CARD-NUM");
      case "carddata.CARD-NUM -> cardxref.XREF-CARD-NUM" ->
          rel("carddata", "CARD-NUM", "cardxref", "XREF-CARD-NUM");
      case "carddata.CARD-ACCT-ID -> acctdata.ACCT-ID" ->
          rel("carddata", "CARD-ACCT-ID", "acctdata", "ACCT-ID");
      case "dailytran.DALYTRAN-CARD-NUM -> cardxref.XREF-CARD-NUM" ->
          rel("dailytran", "DALYTRAN-CARD-NUM", "cardxref", "XREF-CARD-NUM");
      case "dailytran.DALYTRAN-TYPE-CD -> trantype.TRAN-TYPE" ->
          rel("dailytran", "DALYTRAN-TYPE-CD", "trantype", "TRAN-TYPE");
      case "dailytran.(TYPE-CD,CAT-CD) -> trancatg" ->
          rel("dailytran", List.of("DALYTRAN-TYPE-CD", "DALYTRAN-CAT-CD"), "trancatg",
              List.of("TRAN-TYPE-CD", "TRAN-CAT-CD"));
      case "trancatg.TRAN-TYPE-CD -> trantype.TRAN-TYPE" ->
          rel("trancatg", "TRAN-TYPE-CD", "trantype", "TRAN-TYPE");
      case "tcatbal.TRANCAT-ACCT-ID -> acctdata.ACCT-ID" ->
          rel("tcatbal", "TRANCAT-ACCT-ID", "acctdata", "ACCT-ID");
      case "tcatbal.(TYPE-CD,CD) -> trancatg" ->
          rel("tcatbal", List.of("TRANCAT-TYPE-CD", "TRANCAT-CD"), "trancatg",
              List.of("TRAN-TYPE-CD", "TRAN-CAT-CD"));
      case "discgrp.(TYPE-CD,CAT-CD) -> trancatg" ->
          rel("discgrp", List.of("DIS-TRAN-TYPE-CD", "DIS-TRAN-CAT-CD"), "trancatg",
              List.of("TRAN-TYPE-CD", "TRAN-CAT-CD"));
      case "acctdata.ACCT-GROUP-ID -> discgrp.DIS-ACCT-GROUP-ID" ->
          rel("acctdata", "ACCT-GROUP-ID", "discgrp", "DIS-ACCT-GROUP-ID");
      case "acctdata.ACCT-ADDR-ZIP -> discgrp.DIS-ACCT-GROUP-ID (seed-data quirk probe)" ->
          rel("acctdata", "ACCT-ADDR-ZIP", "discgrp", "DIS-ACCT-GROUP-ID");
      case "acctdata.ACCT-ID -> cardxref.XREF-ACCT-ID" ->
          rel("acctdata", "ACCT-ID", "cardxref", "XREF-ACCT-ID");
      case "custdata.CUST-ID -> cardxref.XREF-CUST-ID" ->
          rel("custdata", "CUST-ID", "cardxref", "XREF-CUST-ID");
      default -> throw new IllegalArgumentException("Unknown referential check " + check);
    };
  }

  private Relationship rel(String childDataset, String childField, String parentDataset,
      String parentField) {
    return rel(childDataset, List.of(childField), parentDataset, List.of(parentField));
  }

  private Relationship rel(String childDataset, List<String> childFields, String parentDataset,
      List<String> parentFields) {
    return new Relationship(childDataset, childFields, parentDataset, parentFields);
  }

  private String keyValue(String dataset, Map<String, Object> record, List<String> keyFields) {
    return keyFields.stream().map(name -> {
      String value = String.valueOf(record.get(name));
      FieldDef field = findField(CardDemoLayouts.get(dataset).fields(), name);
      if (field != null && field.usage() == com.carddemo.harness.codec.Usage.DISPLAY
          && !field.pic().alphanumeric() && !field.pic().signed()) {
        return "0".repeat(Math.max(0, field.pic().digitCount() - value.length())) + value;
      }
      return value;
    }).collect(Collectors.joining());
  }

  private FieldDef findField(List<FieldDef> fields, String name) {
    for (FieldDef field : fields) {
      if (field.name().equals(name)) {
        return field;
      }
      FieldDef nested = findField(field.children(), name);
      if (nested != null) {
        return nested;
      }
    }
    return null;
  }

  private Object getValue(Map<String, Object> record, String field) {
    if (record.containsKey(field)) {
      return record.get(field);
    }
    for (Object value : record.values()) {
      if (value instanceof Map<?, ?> nested) {
        Object found = getValue(GoldenDataLoader.object(nested), field);
        if (found != null) {
          return found;
        }
      } else if (value instanceof List<?> list) {
        for (Object item : list) {
          if (item instanceof Map<?, ?> nested) {
            Object found = getValue(GoldenDataLoader.object(nested), field);
            if (found != null) {
              return found;
            }
          }
        }
      }
    }
    return null;
  }

  private void collectNumericFields(List<FieldDef> fields, List<FieldDef> result) {
    for (FieldDef field : fields) {
      if (field.isGroup()) {
        collectNumericFields(field.children(), result);
      } else if (!field.pic().alphanumeric()) {
        result.add(field);
      }
    }
  }

  private String sha256(byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }

  private record Relationship(String childDataset, List<String> childFields, String parentDataset,
                              List<String> parentFields) {}
}
