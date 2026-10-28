package com.carddemo.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.harness.codec.CardDemoLayouts;
import com.carddemo.harness.codec.RecordCodec;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ByteRoundTripTest {
  static java.util.stream.Stream<String> datasets() {
    return HarnessTestData.DATASETS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("datasets")
  void everySourceRecordRoundTripsByteExactly(String dataset) throws Exception {
    var raw = HarnessTestData.source(dataset);
    var codec = new RecordCodec(CardDemoLayouts.get(dataset));
    for (int index = 0; index < raw.records().size(); index++) {
      byte[] expected = raw.records().get(index);
      byte[] actual = codec.encode(codec.decode(expected, HarnessTestData.sourceEncoding(dataset)),
          HarnessTestData.sourceEncoding(dataset));
      assertThat(actual).as("%s record %s", dataset, index).containsExactly(expected);
    }
  }
}
