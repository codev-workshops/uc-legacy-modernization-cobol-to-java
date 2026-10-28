package com.carddemo.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.harness.codec.CardDemoLayouts;
import com.carddemo.harness.codec.FieldDef;
import com.carddemo.harness.codec.Pic;
import com.carddemo.harness.codec.RecordCodec;
import com.carddemo.harness.compare.ByteRoundTripChecker;
import com.carddemo.harness.compare.ComparisonPolicy;
import com.carddemo.harness.compare.RecordComparator;
import com.carddemo.harness.compare.Canonical;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class MutationDetectionTest {
  private static final Pattern TOKEN = Pattern.compile("([A-Za-z0-9_-]+)|\\[(\\d+)\\]");

  @Test
  void oneRotatingFieldMutationPerGoldenRecordProducesOneDiff() throws Exception {
    RecordComparator comparator = new RecordComparator();
    for (String dataset : HarnessTestData.DATASETS) {
      RecordLayoutData layout = new RecordLayoutData(CardDemoLayouts.get(dataset));
      List<Map<String, Object>> records = HarnessTestData.golden(dataset);
      for (int index = 0; index < records.size(); index++) {
        Map<String, Object> original = records.get(index);
        List<Leaf> leaves = leaves(layout, original);
        assertThat(leaves).as("%s record %s has mutable values", dataset, index).isNotEmpty();
        Leaf leaf = leaves.get(index % leaves.size());
        Object mutated = mutate(leaf.value(), leaf.field().pic());
        Map<String, Object> modified = copy(original);
        replace(modified, leaf.path(), mutated);
        var result = comparator.compare(original, modified, ComparisonPolicy.strict(dataset));
        assertThat(result.differences()).as("%s record %s mutation %s", dataset, index, leaf.path())
            .hasSize(1);
        assertThat(result.differences().get(0).path()).isEqualTo(leaf.path());
      }
    }
  }

  @Test
  void signFlipAndRawSignNibbleCorruptionAreDetected() throws Exception {
    Map<String, Object> original = HarnessTestData.golden("dailytran").get(0);
    Map<String, Object> mutated = copy(original);
    String amount = String.valueOf(original.get("DALYTRAN-AMT"));
    mutated.put("DALYTRAN-AMT", amount.startsWith("-") ? amount.substring(1) : "-" + amount);
    var differences = new RecordComparator().compare(original, mutated, ComparisonPolicy.strict("sign"));
    assertThat(differences.differences()).extracting("path").containsExactly("DALYTRAN-AMT");

    byte[] raw = HarnessTestData.ascii("acctdata").records().get(0).clone();
    FieldDef amountField = CardDemoLayouts.get("acctdata").field("ACCT-CURR-BAL");
    raw[amountField.offset() + amountField.length() - 1] = (byte) '0';
    List<ByteRoundTripChecker.ByteDifference> byteDiffs =
        new ByteRoundTripChecker().check(raw, CardDemoLayouts.get("acctdata"),
            com.carddemo.harness.codec.SourceEncoding.ASCII);
    assertThat(byteDiffs).isNotEmpty();
    assertThat(byteDiffs).anySatisfy(diff -> assertThat(diff.fieldName()).isEqualTo("ACCT-CURR-BAL"));
  }

  private List<Leaf> leaves(RecordLayoutData layout, Map<String, Object> record) {
    List<Leaf> result = new ArrayList<>();
    collect(layout.layout().fields(), record, "", result);
    return result;
  }

  private void collect(List<FieldDef> fields, Map<String, Object> values, String prefix,
      List<Leaf> result) {
    for (FieldDef field : fields) {
      if (field.redefines() != null) {
        Object branch = values.get(field.name());
        if (branch instanceof Map<?, ?> branchMap) {
          collect(field.children(), copy(branchMap), append(prefix, field.name()), result);
        }
        continue;
      }
      if (field.name().equals("EXPORT-RECORD-DATA")) {
        continue;
      }
      if (field.isGroup() && field.occurs() > 1) {
        Object items = values.get(field.name());
        if (items instanceof List<?> list) {
          for (int index = 0; index < list.size(); index++) {
            Object item = list.get(index);
            if (item instanceof Map<?, ?> nested) {
              collect(field.children(), copy(nested),
                  append(prefix, field.name() + "[" + index + "]"), result);
            }
          }
        }
      } else if (field.isGroup()) {
        collect(field.children(), values, prefix, result);
      } else if (values.get(field.name()) instanceof String value) {
        result.add(new Leaf(append(prefix, field.name()), value, field));
      }
    }
  }

  private Object mutate(Object value, Pic pic) {
    if (pic.alphanumeric()) {
      String text = String.valueOf(value);
      char replacement = text.charAt(0) == 'X' ? 'Y' : 'X';
      return replacement + text.substring(1);
    }
    BigDecimal increment = BigDecimal.ONE.movePointLeft(pic.scale());
    return Canonical.of(new BigDecimal(String.valueOf(value)).add(increment), pic.scale());
  }

  private Map<String, Object> copy(Map<?, ?> value) {
    Map<String, Object> result = new LinkedHashMap<>();
    value.forEach((key, item) -> result.put(String.valueOf(key), copyValue(item)));
    return result;
  }

  private Object copyValue(Object value) {
    if (value instanceof Map<?, ?> map) {
      return copy(map);
    }
    if (value instanceof List<?> list) {
      List<Object> copy = new ArrayList<>();
      list.forEach(item -> copy.add(copyValue(item)));
      return copy;
    }
    return value;
  }

  private void replace(Map<String, Object> record, String path, Object value) {
    Matcher matcher = TOKEN.matcher(path);
    List<String> tokens = new ArrayList<>();
    while (matcher.find()) {
      tokens.add(matcher.group(1) == null ? "[" + matcher.group(2) + "]" : matcher.group(1));
    }
    set(record, tokens, 0, value);
  }

  @SuppressWarnings("unchecked")
  private void set(Object current, List<String> tokens, int index, Object value) {
    String token = tokens.get(index);
    boolean last = index == tokens.size() - 1;
    if (token.startsWith("[")) {
      List<Object> list = (List<Object>) current;
      int position = Integer.parseInt(token.substring(1, token.length() - 1));
      if (last) {
        list.set(position, value);
      } else {
        set(list.get(position), tokens, index + 1, value);
      }
    } else {
      Map<String, Object> map = (Map<String, Object>) current;
      if (last) {
        map.put(token, value);
      } else {
        set(map.get(token), tokens, index + 1, value);
      }
    }
  }

  private String append(String prefix, String field) {
    return prefix.isEmpty() ? field : prefix + "." + field;
  }

  private record Leaf(String path, String value, FieldDef field) {}

  private record RecordLayoutData(com.carddemo.harness.codec.RecordLayout layout) {}
}
