package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.AccountExtractJob.Datasets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Output of one run of the Java port. */
record JavaRun(
    byte[] outFile,
    byte[] arryFile,
    byte[] vbrcFile,
    String sysout,
    AccountExtractJob.Summary summary,
    AbendException abend) {

  static JavaRun of(Path input, Path workDir, JobOptions options) {
    Datasets datasets =
        new Datasets(
            input,
            workDir.resolve("OUTFILE"),
            workDir.resolve("ARRYFILE"),
            workDir.resolve("VBRCFILE"));
    ByteArrayOutputStream log = new ByteArrayOutputStream();
    AccountExtractJob.Summary summary = null;
    AbendException abend = null;
    try (PrintStream sysout = new PrintStream(log, true, StandardCharsets.ISO_8859_1)) {
      summary = new AccountExtractJob(options, sysout).run(datasets);
    } catch (AbendException e) {
      abend = e;
    }
    return new JavaRun(
        readIfPresent(datasets.outFile()),
        readIfPresent(datasets.arryFile()),
        readIfPresent(datasets.vbrcFile()),
        log.toString(StandardCharsets.ISO_8859_1),
        summary,
        abend);
  }

  private static byte[] readIfPresent(Path path) {
    try {
      return Files.exists(path) ? Files.readAllBytes(path) : new byte[0];
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
