package com.carddemo.batch.cbact01c;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Cbact01cApplicationTest {

  private static int run(String... args) {
    PrintStream sysout = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
    return Cbact01cApplication.run(args, sysout);
  }

  @Test
  void runsTheSampleAndMatchesTheOracle(@TempDir Path dir) throws Exception {
    int rc =
        run(
            OracleOutput.SAMPLE_INPUT.toString(),
            dir.resolve("o").toString(),
            dir.resolve("a").toString(),
            dir.resolve("v").toString(),
            "--rdw=gnucobol");
    assertEquals(0, rc);
    OracleOutput cobol = OracleOutput.load("sample");
    assertArrayEquals(cobol.outFile(), Files.readAllBytes(dir.resolve("o")));
    assertArrayEquals(cobol.vbrcFile(), Files.readAllBytes(dir.resolve("v")));
  }

  @Test
  void abendReturns16(@TempDir Path dir) {
    String missing = dir.resolve("missing").toString();
    assertEquals(Cbact01cApplication.ABEND_EXIT, run(missing, missing, missing, missing));
  }

  @Test
  void badArgumentsReturnUsageError() {
    assertEquals(Cbact01cApplication.USAGE_EXIT, run("only-one"));
    assertEquals(Cbact01cApplication.USAGE_EXIT, run("a", "b", "c", "d", "--rdw=bogus"));
    assertEquals(Cbact01cApplication.USAGE_EXIT, run("a", "b", "c", "d", "--nope"));
  }
}
