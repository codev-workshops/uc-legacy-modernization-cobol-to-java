package com.carddemo.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.harness.codec.CardDemoLayouts;
import com.carddemo.harness.codec.RecordCodec;
import com.carddemo.harness.codec.SourceEncoding;
import com.carddemo.harness.loader.GoldenDataLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class EbcdicParityTest {
  static java.util.stream.Stream<String> datasets() {
    return HarnessTestData.DATASETS.stream().filter(dataset -> !dataset.equals("export"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("datasets")
  void ebcdicCounterpartMatchesAsciiExceptDeclaredDivergences(String dataset) throws Exception {
    var counterpart = GoldenDataLoader.object(
        HarnessTestData.datasetManifest(dataset).get("ebcdicCounterpart"));
    var raw = HarnessTestData.ebcdicCounterpart(dataset);
    var codec = new RecordCodec(CardDemoLayouts.get(dataset));
    var decoded = raw.records().stream()
        .map(record -> codec.decode(record, SourceEncoding.EBCDIC_CP037)).toList();
    var ascii = HarnessTestData.golden(dataset);
    assertThat(decoded).hasSameSizeAs(ascii);

    Map<Integer, Set<String>> allowed = new HashMap<>();
    for (Object row : (java.util.List<?>) counterpart.get("divergentRecords")) {
      Map<String, Object> divergence = GoldenDataLoader.object(row);
      Set<String> fields = new HashSet<>();
      ((java.util.List<?>) divergence.get("fields")).forEach(field -> fields.add(String.valueOf(field)));
      allowed.put(((Number) divergence.get("recordIndex")).intValue(), fields);
    }
    for (int index = 0; index < ascii.size(); index++) {
      Set<String> different = new HashSet<>();
      for (String key : ascii.get(index).keySet()) {
        if (!java.util.Objects.equals(ascii.get(index).get(key), decoded.get(index).get(key))) {
          different.add(key);
        }
      }
      assertThat(different).as("%s record %s divergences", dataset, index)
          .isEqualTo(allowed.getOrDefault(index, Set.of()));
    }
  }
}
