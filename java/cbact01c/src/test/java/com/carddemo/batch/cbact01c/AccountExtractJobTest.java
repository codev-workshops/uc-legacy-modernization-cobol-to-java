package com.carddemo.batch.cbact01c;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carddemo.batch.cbact01c.JobOptions.CycleDebitPolicy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Business rules of CBACT01C, checked on hand-built records. */
class AccountExtractJobTest {

  private static final int DEBIT_OFFSET = 11 + 1 + 12 * 3 + 10 * 3 + 12;

  private static String record(String id, String balance, String cycleDebit) {
    String fields =
        id
            + "Y"
            + balance
            + "00000500000{"
            + "00000100000{"
            + "2020-01-012030-12-312026-07-04"
            + "00000000500{"
            + cycleDebit
            + "11111     "
            + "GOLD      ";
    return fields + " ".repeat(Account.RECORD_LENGTH - fields.length());
  }

  private static JavaRun run(Path dir, CycleDebitPolicy policy, String... records)
      throws IOException {
    Path input = dir.resolve("acct.txt");
    Files.write(input, List.of(records), StandardCharsets.ISO_8859_1);
    return JavaRun.of(input, dir, new JobOptions(RdwFormat.ZOS, policy));
  }

  private static String debitHex(byte[] outFile, int record) {
    int start = record * AccountOutputRecord.RECORD_LENGTH + DEBIT_OFFSET;
    return HexFormat.of().withUpperCase().formatHex(outFile, start, start + 7);
  }

  @Test
  void zeroCycleDebitBecomes2525(@TempDir Path dir) throws IOException {
    JavaRun run =
        run(dir, CycleDebitPolicy.LEGACY, record("00000000001", "00000001940{", "00000000000{"));
    assertEquals("0000000252500C", debitHex(run.outFile(), 0));
  }

  @Test
  void legacyCarriesThePreviousDebitForwardForNonZeroDebits(@TempDir Path dir) throws IOException {
    JavaRun run =
        run(
            dir,
            CycleDebitPolicy.LEGACY,
            record("00000000001", "00000001940{", "00000000123C"),
            record("00000000002", "00000001940{", "00000000000{"),
            record("00000000003", "00000001940{", "00000000999I"));
    assertAll(
        () -> assertEquals("00000000000000", debitHex(run.outFile(), 0), "low-values"),
        () -> assertEquals("0000000252500C", debitHex(run.outFile(), 1)),
        () -> assertEquals("0000000252500C", debitHex(run.outFile(), 2), "carried forward"));
  }

  @Test
  void copySourcePolicyWritesTheAccountsDebit(@TempDir Path dir) throws IOException {
    JavaRun run =
        run(
            dir,
            CycleDebitPolicy.COPY_SOURCE,
            record("00000000001", "00000001940{", "00000000123L"),
            record("00000000002", "00000001940{", "00000000000}"));
    assertAll(
        () -> assertEquals("0000000001233D", debitHex(run.outFile(), 0)),
        () -> assertEquals("0000000252500C", debitHex(run.outFile(), 1), "negative zero is zero"));
  }

  @Test
  void arrayRecordHoldsTwoBalancesAndConstants() {
    Account account = Account.parse(record("00000000007", "00000000099R", "00000000000{"));
    AccountArrayRecord array = AccountExtractJob.toArrayRecord(account);
    List<AccountArrayRecord.Balance> balances = array.balances();
    assertAll(
        () -> assertEquals(account.currentBalance(), balances.get(0).currentBalance()),
        () -> assertEquals(SignedAmount.of("1005.00"), balances.get(0).currentCycleDebit()),
        () -> assertEquals(account.currentBalance(), balances.get(1).currentBalance()),
        () -> assertEquals(SignedAmount.of("1525.00"), balances.get(1).currentCycleDebit()),
        () -> assertEquals(SignedAmount.of("-1025.00"), balances.get(2).currentBalance()),
        () -> assertEquals(SignedAmount.of("-2500.00"), balances.get(2).currentCycleDebit()),
        () -> assertEquals(AccountArrayRecord.Balance.INITIAL, balances.get(3)),
        () -> assertEquals(AccountArrayRecord.Balance.INITIAL, balances.get(4)));
    byte[] bytes = array.toBytes();
    assertEquals(AccountArrayRecord.RECORD_LENGTH, bytes.length);
    assertEquals(
        "    ", new String(bytes, 106, 4, StandardCharsets.ISO_8859_1), "ARR-FILLER is spaces");
  }

  @Test
  void recordsAreWrittenInKeyOrder(@TempDir Path dir) throws IOException {
    JavaRun run =
        run(
            dir,
            CycleDebitPolicy.LEGACY,
            record("00000000900", "00000001940{", "00000000000{"),
            record("00000000005", "00000001940{", "00000000000{"));
    String first = new String(run.outFile(), 0, 11, StandardCharsets.ISO_8859_1);
    assertEquals("00000000005", first);
    assertEquals(2, run.summary().accountsRead());
  }

  @Test
  void reissueDateIsCompactedAndYearGoesToVb2(@TempDir Path dir) throws IOException {
    JavaRun run =
        run(dir, CycleDebitPolicy.LEGACY, record("00000000001", "00000001940{", "00000000000{"));
    String out = new String(run.outFile(), StandardCharsets.ISO_8859_1);
    assertEquals("20260704  ", out.substring(11 + 1 + 36 + 20, 11 + 1 + 36 + 30));
    byte[] vb = run.vbrcFile();
    assertArrayEquals(new byte[] {0, 16, 0, 0}, Arrays.copyOfRange(vb, 0, 4), "RDW 12 + 4");
    assertArrayEquals(new byte[] {0, 43, 0, 0}, Arrays.copyOfRange(vb, 16, 20), "RDW 39 + 4");
    assertEquals("2026", new String(vb, 20 + 35, 4, StandardCharsets.ISO_8859_1));
  }

  @Test
  void invalidNumericDataIsADataException(@TempDir Path dir) throws IOException {
    JavaRun run =
        run(dir, CycleDebitPolicy.LEGACY, record("00000000001", "0000000194 {", "00000000000{"));
    assertNotNull(run.abend());
    assertEquals(AbendException.DATA_EXCEPTION, run.abend().abendCode());
  }

  @Test
  void overlongRecordIsAnAttributeConflict(@TempDir Path dir) throws IOException {
    JavaRun run = run(dir, CycleDebitPolicy.LEGACY, "0".repeat(301));
    assertEquals(FileStatus.ATTRIBUTE_CONFLICT, run.abend().fileStatus());
    assertTrue(run.sysout().contains("ERROR OPENING ACCTFILE"));
    assertTrue(run.sysout().contains("FILE STATUS IS: NNNN0039"));
  }

  @Test
  void duplicateKeyIsRejected(@TempDir Path dir) throws IOException {
    String rec = record("00000000001", "00000001940{", "00000000000{");
    JavaRun run = run(dir, CycleDebitPolicy.LEGACY, rec, rec);
    assertEquals(FileStatus.ATTRIBUTE_CONFLICT, run.abend().fileStatus());
  }

  @Test
  void unwritableOutputAbendsWithOpenError(@TempDir Path dir) throws IOException {
    Path input = dir.resolve("acct.txt");
    Files.writeString(input, record("00000000001", "00000001940{", "00000000000{"));
    AccountExtractJob.Datasets datasets =
        new AccountExtractJob.Datasets(
            input, dir.resolve("missing/OUTFILE"), dir.resolve("A"), dir.resolve("V"));
    ByteArrayOutputStream log = new ByteArrayOutputStream();
    AccountExtractJob job =
        new AccountExtractJob(
            JobOptions.DEFAULTS, new PrintStream(log, true, StandardCharsets.ISO_8859_1));
    AbendException e = assertThrows(AbendException.class, () -> job.run(datasets));
    assertEquals(FileStatus.PERMANENT_ERROR, e.fileStatus());
    assertEquals(
        String.join(
            "\n",
            "START OF EXECUTION OF PROGRAM CBACT01C",
            "ERROR OPENING OUTFILE30",
            "FILE STATUS IS: NNNN0030",
            "ABENDING PROGRAM",
            ""),
        log.toString(StandardCharsets.ISO_8859_1));
  }
}
