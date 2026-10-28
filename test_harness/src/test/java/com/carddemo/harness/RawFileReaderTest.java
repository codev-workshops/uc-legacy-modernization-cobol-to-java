package com.carddemo.harness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carddemo.harness.loader.RawFileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RawFileReaderTest {
  @TempDir Path temporaryDirectory;

  @Test
  void readsLfCrLfAndUnterminatedLinesWithPadding() throws IOException {
    Path input = temporaryDirectory.resolve("records.txt");
    Files.write(input, "ab\nc\r\nd".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    var records = new RawFileReader().readAscii(input, 3);
    assertThat(records.records()).hasSize(3);
    assertThat(new String(records.records().get(0), java.nio.charset.StandardCharsets.US_ASCII))
        .isEqualTo("ab ");
    assertThat(new String(records.records().get(1), java.nio.charset.StandardCharsets.US_ASCII))
        .isEqualTo("c  ");
    assertThat(new String(records.records().get(2), java.nio.charset.StandardCharsets.US_ASCII))
        .isEqualTo("d  ");
    assertThat(records.terminators()).containsEntry("LF", 1).containsEntry("CRLF", 1)
        .containsEntry("NONE", 1);
    assertThat(records.paddedRecords()).isEqualTo(3);
  }

  @Test
  void rejectsOverlongLinesAndNonMultipleFixedBlocks() throws IOException {
    Path input = temporaryDirectory.resolve("invalid.txt");
    Files.writeString(input, "toolong\n");
    assertThatThrownBy(() -> new RawFileReader().readAscii(input, 3))
        .isInstanceOf(IOException.class).hasMessageContaining("length 7 > LRECL 3");
    Files.write(input, new byte[] {1, 2, 3});
    assertThatThrownBy(() -> new RawFileReader().readFixed(input, 2))
        .isInstanceOf(IOException.class).hasMessageContaining("not a multiple of 2");
  }
}
