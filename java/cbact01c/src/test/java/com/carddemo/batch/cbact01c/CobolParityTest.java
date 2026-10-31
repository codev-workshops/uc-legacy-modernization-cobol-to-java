package com.carddemo.batch.cbact01c;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.carddemo.batch.cbact01c.JobOptions.CycleDebitPolicy;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Differential tests: the Java port against the unmodified CBACT01C run under GnuCOBOL. The golden
 * files in {@code src/test/resources/golden} are produced by {@code
 * src/test/cobol/regenerate-golden.sh}; {@link LiveCobolOracleTest} checks they are current.
 */
class CobolParityTest {

  private static final JobOptions ORACLE_OPTIONS =
      new JobOptions(RdwFormat.GNUCOBOL, CycleDebitPolicy.LEGACY);

  static Stream<Arguments> scenarios() {
    return Stream.of(
        Arguments.of("sample", OracleOutput.SAMPLE_INPUT, 50),
        Arguments.of("edge", OracleOutput.INPUT.resolve("acctdata-edge.txt"), 7),
        Arguments.of("empty", OracleOutput.INPUT.resolve("acctdata-empty.txt"), 0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  void outfileIsByteIdentical(String scenario, Path input, int accounts, @TempDir Path dir) {
    JavaRun java = JavaRun.of(input, dir, ORACLE_OPTIONS);
    OracleOutput cobol = OracleOutput.load(scenario);
    assertEquals(accounts * AccountOutputRecord.RECORD_LENGTH, cobol.outFile().length);
    assertArrayEquals(cobol.outFile(), java.outFile());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  void vbrcfileIsByteIdentical(String scenario, Path input, int accounts, @TempDir Path dir) {
    JavaRun java = JavaRun.of(input, dir, ORACLE_OPTIONS);
    OracleOutput cobol = OracleOutput.load(scenario);
    List<byte[]> records = OracleOutput.logicalRecords(cobol.vbrcFile(), RdwFormat.GNUCOBOL);
    assertEquals(accounts * 2, records.size());
    assertArrayEquals(cobol.vbrcFile(), java.vbrcFile());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  void arryfileMatchesExceptInitializedZeroSign(
      String scenario, Path input, int accounts, @TempDir Path dir) {
    JavaRun java = JavaRun.of(input, dir, ORACLE_OPTIONS);
    OracleOutput cobol = OracleOutput.load(scenario);
    assertEquals(accounts * AccountArrayRecord.RECORD_LENGTH, cobol.arryFile().length);
    assertArrayEquals(cobol.arryFileAsZos(), java.arryFile());
    assertEquals(
        differingBytes(cobol.arryFile(), java.arryFile()),
        accounts * OracleOutput.INITIALIZED_BALANCE_SIGN_OFFSETS.length,
        "only the sign byte of ARR-ACCT-CURR-BAL(4) and (5) may differ");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  void sysoutMatches(String scenario, Path input, int accounts, @TempDir Path dir) {
    JavaRun java = JavaRun.of(input, dir, ORACLE_OPTIONS);
    OracleOutput cobol = OracleOutput.load(scenario);
    assertAll(
        () -> assertEquals(0, cobol.rc()),
        () -> assertNull(java.abend()),
        () -> assertEquals(cobol.sysoutAsZos(), java.sysout()),
        () -> assertEquals(accounts, java.summary().accountsRead()),
        () -> assertEquals(accounts * 2, java.summary().variableRecords()));
  }

  @Test
  void zosRdwFileHoldsTheSameLogicalRecords(@TempDir Path dir) {
    JavaRun java =
        JavaRun.of(
            OracleOutput.SAMPLE_INPUT, dir, new JobOptions(RdwFormat.ZOS, CycleDebitPolicy.LEGACY));
    List<byte[]> expected =
        OracleOutput.logicalRecords(OracleOutput.load("sample").vbrcFile(), RdwFormat.GNUCOBOL);
    List<byte[]> actual = OracleOutput.logicalRecords(java.vbrcFile(), RdwFormat.ZOS);
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertArrayEquals(expected.get(i), actual.get(i), "record " + i);
    }
    assertEquals(0x10, java.vbrcFile()[1], "RDW of a 12-byte record is 16");
  }

  @Test
  void missingInputAbendsLikeCobol(@TempDir Path dir) {
    JavaRun java = JavaRun.of(dir.resolve("no-such-file"), dir, ORACLE_OPTIONS);
    OracleOutput cobol = OracleOutput.load("missing-input");
    assertAll(
        () -> assertEquals(999 % 256, cobol.rc()),
        () -> assertNotNull(java.abend()),
        () -> assertEquals(AbendException.USER_ABEND, java.abend().abendCode()),
        () -> assertEquals(FileStatus.FILE_NOT_FOUND, java.abend().fileStatus()),
        () -> assertEquals(cobol.sysoutAsZos(), java.sysout()));
  }

  private static int differingBytes(byte[] a, byte[] b) {
    int count = 0;
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) {
        count++;
      }
    }
    return count;
  }
}
