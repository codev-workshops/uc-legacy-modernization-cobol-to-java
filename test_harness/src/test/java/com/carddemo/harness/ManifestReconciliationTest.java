package com.carddemo.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.harness.loader.GoldenDataLoader;
import com.carddemo.harness.recon.ReconciliationEngine;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ManifestReconciliationTest {
  @Test
  void countsTotalsKeysHashesAndOrphanCountsMatchManifest() throws Exception {
    Map<String, List<Map<String, Object>>> records = new LinkedHashMap<>();
    for (String dataset : HarnessTestData.DATASETS) {
      records.put(dataset, HarnessTestData.golden(dataset));
    }
    Map<String, Object> manifest = HarnessTestData.manifest();
    var report = new ReconciliationEngine().reconcile(records, manifest);
    Map<String, Object> datasets = GoldenDataLoader.object(manifest.get("datasets"));
    for (String dataset : HarnessTestData.DATASETS) {
      Map<String, Object> metadata = GoldenDataLoader.object(datasets.get(dataset));
      var actual = report.datasets().get(dataset);
      var raw = HarnessTestData.source(dataset);
      assertThat(actual.recordCount()).isEqualTo(((Number) metadata.get("recordCount")).intValue());
      assertThat(raw.records()).hasSize(actual.recordCount());
      assertThat(raw.originalBytes()).hasSize(((Number) metadata.get("sourceBytes")).intValue());
      assertThat(raw.paddedRecords()).isEqualTo(metadata.get("paddedRecordCount") == null
          ? 0 : ((Number) metadata.get("paddedRecordCount")).intValue());
      if (metadata.containsKey("lineTerminators")) {
        assertThat(raw.terminators()).isEqualTo(metadata.get("lineTerminators"));
      }
      assertThat(actual.controlTotals()).isEqualTo(metadata.get("controlTotals"));
      assertThat(actual.keyMin()).isEqualTo(String.valueOf(metadata.get("keyMin")));
      assertThat(actual.keyMax()).isEqualTo(String.valueOf(metadata.get("keyMax")));
      assertThat(actual.keyUnique()).isEqualTo(metadata.get("keyUnique"));
      assertThat(actual.duplicateKeyCount()).isEqualTo(metadata.get("duplicateKeyCount"));
      assertThat(actual.sourceSha256()).isEqualTo(metadata.get("sourceSha256"));
    }
    List<Map<String, Object>> expected = (List<Map<String, Object>>) manifest.get("referentialIntegrity");
    assertThat(report.referentialIntegrity()).hasSameSizeAs(expected);
    for (int index = 0; index < expected.size(); index++) {
      var actual = report.referentialIntegrity().get(index);
      Map<String, Object> item = expected.get(index);
      assertThat(actual.check()).isEqualTo(item.get("check"));
      assertThat(actual.childRecords()).isEqualTo(item.get("childRecords"));
      assertThat(actual.orphanRecords()).isEqualTo(item.get("orphanRecords"));
      assertThat(actual.distinctOrphanKeys()).isEqualTo(item.get("distinctOrphanKeys"));
    }
  }
}
