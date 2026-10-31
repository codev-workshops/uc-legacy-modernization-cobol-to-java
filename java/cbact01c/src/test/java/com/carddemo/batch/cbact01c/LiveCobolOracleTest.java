package com.carddemo.batch.cbact01c;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Recompiles and reruns the COBOL program with GnuCOBOL and checks the committed golden files are
 * what it produces today. Skipped when {@code cobc} is not installed.
 */
class LiveCobolOracleTest {

  private static final Path ORACLE = Path.of("src/test/cobol/run-oracle.sh");

  @ParameterizedTest
  @ValueSource(strings = {"sample", "edge"})
  void goldenFilesAreCurrent(String scenario, @TempDir Path dir) throws Exception {
    assumeTrue(cobcAvailable(), "GnuCOBOL (cobc) is not installed");
    Path input =
        scenario.equals("sample")
            ? OracleOutput.SAMPLE_INPUT
            : OracleOutput.INPUT.resolve("acctdata-edge.txt");
    Process process =
        new ProcessBuilder("bash", ORACLE.toString(), input.toString(), dir.toString())
            .inheritIO()
            .start();
    assertEquals(0, process.waitFor());
    OracleOutput live = OracleOutput.load(dir);
    OracleOutput golden = OracleOutput.load(scenario);
    assertAll(
        () -> assertArrayEquals(golden.outFile(), live.outFile()),
        () -> assertArrayEquals(golden.arryFile(), live.arryFile()),
        () -> assertArrayEquals(golden.vbrcFile(), live.vbrcFile()),
        () -> assertEquals(golden.sysout(), live.sysout()),
        () -> assertEquals(golden.rc(), live.rc()));
  }

  private static boolean cobcAvailable() {
    try {
      Process p = new ProcessBuilder("cobc", "--version").redirectErrorStream(true).start();
      p.getInputStream().readAllBytes();
      return p.waitFor(30, TimeUnit.SECONDS) && p.exitValue() == 0;
    } catch (IOException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
