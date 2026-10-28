package com.carddemo.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.harness.codec.RecordCodec;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class GoldenDecodeTest {
  static java.util.stream.Stream<String> datasets() {
    return HarnessTestData.DATASETS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("datasets")
  void javaDecoderMatchesReferenceGoldenExactly(String dataset) throws Exception {
    var raw = HarnessTestData.source(dataset);
    var encoding = HarnessTestData.sourceEncoding(dataset);
    var codec = new RecordCodec(com.carddemo.harness.codec.CardDemoLayouts.get(dataset));
    var decoded = raw.records().stream()
        .<Map<String, Object>>map(record -> codec.decode(record, encoding)).toList();
    assertThat(decoded).as("Java/golden disagreement in %s", dataset)
        .containsExactlyElementsOf(HarnessTestData.golden(dataset));
  }
}
