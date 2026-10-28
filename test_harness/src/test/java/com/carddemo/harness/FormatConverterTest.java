package com.carddemo.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.harness.codec.SourceEncoding;
import com.carddemo.harness.loader.FormatConverter;
import org.junit.jupiter.api.Test;

class FormatConverterTest {
  @Test
  void convertsManifestFilesToGoldenRecordValues() throws Exception {
    FormatConverter converter = new FormatConverter();
    assertThat(converter.rawToGolden("acctdata", SourceEncoding.ASCII))
        .containsExactlyElementsOf(HarnessTestData.golden("acctdata"));
    assertThat(converter.rawToGolden("acctdata", SourceEncoding.EBCDIC_CP037))
        .hasSameSizeAs(HarnessTestData.golden("acctdata"));
    assertThat(converter.rawToGolden("export", SourceEncoding.EBCDIC_CP037))
        .containsExactlyElementsOf(HarnessTestData.golden("export"));
  }
}
