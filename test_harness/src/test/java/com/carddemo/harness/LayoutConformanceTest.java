package com.carddemo.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.harness.codec.CardDemoLayouts;
import com.carddemo.harness.codec.FieldDef;
import com.carddemo.harness.codec.RecordLayout;
import com.carddemo.harness.loader.GoldenDataLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LayoutConformanceTest {
  @Test
  void everyDatasetLayoutMatchesManifestStorageRows() throws Exception {
    Map<String, Object> datasets = GoldenDataLoader.object(HarnessTestData.manifest().get("datasets"));
    for (String dataset : HarnessTestData.DATASETS) {
      Map<String, Object> metadata = GoldenDataLoader.object(datasets.get(dataset));
      List<Map<String, Object>> expected = new ArrayList<>();
      for (Object item : (List<?>) metadata.get("layout")) {
        flattenManifest(GoldenDataLoader.object(item), expected);
      }
      List<Map<String, Object>> actual = new ArrayList<>();
      flatten(CardDemoLayouts.get(dataset).fields(), actual);
      assertThat(actual).as("layout %s", dataset).containsExactlyElementsOf(expected);
    }
  }

  @Test
  void outputLayoutsHaveRequiredRecordLengthsAndSequentialStorage() {
    assertSequentialLayout(CardDemoLayouts.get("TRAN-RECORD"), 350);
    assertSequentialLayout(CardDemoLayouts.get("DALYREJS"), 430);
  }

  private void assertSequentialLayout(RecordLayout layout, int expectedLength) {
    int offset = 0;
    for (FieldDef field : layout.fields()) {
      assertThat(field.offset()).as("%s.%s offset", layout.name(), field.name()).isEqualTo(offset);
      offset += field.length() * field.occurs();
    }
    assertThat(layout.recordLength()).isEqualTo(expectedLength);
    assertThat(offset).isEqualTo(expectedLength);
  }

  private void flatten(List<FieldDef> fields, List<Map<String, Object>> rows) {
    for (FieldDef field : fields) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("name", field.name());
      row.put("offset", field.offset() + 1);
      row.put("length", field.length());
      if (!field.isGroup()) {
        row.put("pic", field.pic().source());
        row.put("usage", usage(field));
      }
      if (field.occurs() > 1) {
        row.put("occurs", field.occurs());
      }
      if (field.redefines() != null) {
        row.put("redefines", field.redefines());
      }
      rows.add(row);
      flatten(field.children(), rows);
    }
  }

  private void flattenManifest(Map<String, Object> field, List<Map<String, Object>> rows) {
    Map<String, Object> row = new LinkedHashMap<>();
    for (String key : List.of("name", "offset", "length", "pic", "usage", "occurs", "redefines")) {
      if (field.containsKey(key)) {
        row.put(key, field.get(key));
      }
    }
    rows.add(row);
    Object occursLayout = field.get("occursLayout");
    if (occursLayout instanceof List<?> nested) {
      for (Object item : nested) {
        flattenManifest(GoldenDataLoader.object(item), rows);
      }
    }
  }

  private String usage(FieldDef field) {
    return switch (field.usage()) {
      case COMP_3 -> "COMP-3";
      case COMP_5 -> "COMP-5";
      default -> field.usage().name();
    };
  }
}
