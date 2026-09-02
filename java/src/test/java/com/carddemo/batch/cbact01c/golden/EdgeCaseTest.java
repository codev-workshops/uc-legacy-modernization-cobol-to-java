package com.carddemo.batch.cbact01c.golden;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.codec.Hex;
import com.carddemo.batch.cbact01c.codec.PackedDecimalCodec;
import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrArrayRec;
import com.carddemo.batch.cbact01c.model.OutAcctRec;
import com.carddemo.batch.cbact01c.model.Vb2Rec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Edge cases of the CBACT01C rules that the 50-record sample never exercises. */
class EdgeCaseTest {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final byte[] PACKED_2525 = Hex.bytes("0000000252500C");
    private static final byte[] PACKED_ZERO = Hex.bytes("0000000000000C");
    private static final byte[] PACKED_MINUS_2500 = Hex.bytes("0000000250000D");
    private static final byte[] PACKED_1005 = Hex.bytes("0000000100500C");
    private static final byte[] PACKED_1525 = Hex.bytes("0000000152500C");
    private static final String ZONED_MINUS_1025 = "F0F0F0F0F0F0F1F0F2F5F0D0";
    private static final String ZONED_PLUS_ZERO = "F0F0F0F0F0F0F0F0F0F0F0C0";

    @TempDir
    Path tmp;

    private static AccountRecord account(String id, BigDecimal currBal, BigDecimal debit, String reissueDate) {
        return new AccountRecord(id, "Y", currBal, new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", reissueDate, ZERO, debit, "A000000000", "          ", new byte[0]);
    }

    private ProgramRun run(AccountRecord... accounts) throws IOException {
        ByteArrayOutputStream in = new ByteArrayOutputStream();
        for (AccountRecord a : accounts) {
            in.writeBytes(a.toBytes(CobolCharset.EBCDIC));
        }
        Path acct = tmp.resolve("synthetic-" + in.size() + ".ps");
        Files.write(acct, in.toByteArray());
        ProgramRun run = ProgramRun.execute(acct, tmp.resolve("out-" + in.size()), CobolCharset.EBCDIC);
        assertEquals(0, run.exitCode(), run.console());
        return run;
    }

    private static byte[] outCycDebit(ProgramRun run, int recordIndex) {
        byte[] rec = ProgramRun.fixedRecords(run.outfile(), OutAcctRec.LENGTH).get(recordIndex);
        return Arrays.copyOfRange(rec, 90, 97);
    }

    @Test
    void zeroInputDebitWrites2525() throws IOException {
        ProgramRun run = run(account("00000000001", new BigDecimal("194.00"), ZERO, "2025-05-20"));
        assertArrayEquals(PACKED_2525, outCycDebit(run, 0));
    }

    @Test
    void nonZeroDebitOnTheFirstRecordLeavesPackedZero() throws IOException {
        ProgramRun run = run(account("00000000001", new BigDecimal("194.00"), new BigDecimal("15.00"), "2025-05-20"));
        assertArrayEquals(PACKED_ZERO, outCycDebit(run, 0));
    }

    @Test
    void nonZeroDebitCarriesThePreviousRecordsValue() throws IOException {
        ProgramRun run = run(
                account("00000000001", new BigDecimal("194.00"), ZERO, "2025-05-20"),
                account("00000000002", new BigDecimal("195.00"), new BigDecimal("15.00"), "2025-05-20"),
                account("00000000003", new BigDecimal("196.00"), new BigDecimal("-15.00"), "2025-05-20"),
                account("00000000004", new BigDecimal("197.00"), ZERO, "2025-05-20"));
        assertArrayEquals(PACKED_2525, outCycDebit(run, 0));
        assertArrayEquals(PACKED_2525, outCycDebit(run, 1), "carried from record 1");
        assertArrayEquals(PACKED_2525, outCycDebit(run, 2), "carried from record 2");
        assertArrayEquals(PACKED_2525, outCycDebit(run, 3));
    }

    @Test
    void arrayRecordCarriesHardCodedNegativesAndInitialisedTail() throws IOException {
        ProgramRun run = run(account("00000000007", new BigDecimal("194.00"), ZERO, "2025-05-20"));
        byte[] rec = ProgramRun.fixedRecords(run.arryfile(), ArrArrayRec.LENGTH).get(0);
        assertEquals(ZONED_MINUS_1025, Hex.of(occurrenceZoned(rec, 2)), "occurrence 3 balance -1025.00");
        assertArrayEquals(PACKED_MINUS_2500, occurrencePacked(rec, 2), "occurrence 3 debit -2500.00");
        assertArrayEquals(PACKED_1005, occurrencePacked(rec, 0));
        assertArrayEquals(PACKED_1525, occurrencePacked(rec, 1));
        for (int i : new int[] {3, 4}) {
            assertEquals(ZONED_PLUS_ZERO, Hex.of(occurrenceZoned(rec, i)), "occurrence " + (i + 1) + " balance +0");
            assertArrayEquals(PACKED_ZERO, occurrencePacked(rec, i), "occurrence " + (i + 1) + " debit +0");
        }
        assertEquals("40404040", Hex.of(Arrays.copyOfRange(rec, 106, 110)), "ARR-FILLER");
    }

    @ParameterizedTest
    @CsvSource({
        "0.00,          0000000000000C",
        "0.01,          0000000000001C",
        "-0.01,         0000000000001D",
        "9999999999.99, 0999999999999C",
        "-9999999999.99,0999999999999D",
    })
    void comp3BoundaryValuesRoundTrip(String value, String hex) {
        // 7 bytes carry 13 digit nibbles; PIC S9(10)V99 fills 12, so the leading nibble is a pad zero.
        BigDecimal expected = new BigDecimal(value.trim());
        byte[] encoded = PackedDecimalCodec.encode(expected, 10, 2);
        assertEquals(7, encoded.length);
        assertEquals(hex.trim(), Hex.of(encoded));
        assertEquals(expected, PackedDecimalCodec.decode(encoded, 0, 10, 2));
    }

    @Test
    void variableRecordsUseFourByteRdwsAnd59BytesPerAccount() throws IOException {
        ProgramRun run = run(
                account("00000000001", new BigDecimal("194.00"), ZERO, "2025-05-20"),
                account("00000000002", new BigDecimal("195.00"), ZERO, "1999-12-31"),
                account("00000000003", new BigDecimal("196.00"), ZERO, "2000-02-29"));
        assertEquals(3 * 59, run.vbrcfile().length);
        for (int i = 0; i < 3; i++) {
            int off = i * 59;
            assertEquals("00100000", Hex.of(Arrays.copyOfRange(run.vbrcfile(), off, off + 4)), "VBRC-REC1 RDW");
            assertEquals("002B0000", Hex.of(Arrays.copyOfRange(run.vbrcfile(), off + 16, off + 20)), "VBRC-REC2 RDW");
        }
        List<byte[]> records = ProgramRun.variableRecords(run.vbrcfile());
        assertEquals(6, records.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(12, records.get(2 * i).length);
            assertEquals(39, records.get(2 * i + 1).length);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "2025-05-20, 20250520",
        "1999-12-31, 19991231",
        "2000-02-29, 20000229",
        "2023-13-45, 20231345",
    })
    void reissueDateIsReformattedPositionallyWithoutValidation(String reissue, String expected) throws IOException {
        ProgramRun run = run(account("00000000001", new BigDecimal("194.00"), ZERO, reissue));
        OutAcctRec out = OutAcctRec.fromBytes(
                ProgramRun.fixedRecords(run.outfile(), OutAcctRec.LENGTH).get(0), CobolCharset.EBCDIC);
        assertEquals(expected + "  ", out.reissueDate(), "OUT-ACCT-REISSUE-DATE = YYYYMMDD + fill spaces");
        Vb2Rec vb2 = Vb2Rec.fromBytes(ProgramRun.variableRecords(run.vbrcfile()).get(1), CobolCharset.EBCDIC);
        assertEquals(reissue.substring(0, 4), vb2.reissueYyyy(), "VB2-ACCT-REISSUE-YYYY comes from the input date");
    }

    @Test
    void negativeInputBalanceFlowsToEveryOutputWithSignD() throws IOException {
        BigDecimal negative = new BigDecimal("-194.00");
        ProgramRun run = run(account("00000000001", negative, ZERO, "2025-05-20"));
        String zonedNegative = "F0F0F0F0F0F0F0F1F9F4F0D0";  // "00000001940" + '}' (D0)

        byte[] outRec = ProgramRun.fixedRecords(run.outfile(), OutAcctRec.LENGTH).get(0);
        assertEquals(zonedNegative, Hex.of(Arrays.copyOfRange(outRec, 12, 24)), "OUT-ACCT-CURR-BAL");
        assertEquals(negative, OutAcctRec.fromBytes(outRec, CobolCharset.EBCDIC).currBal());

        byte[] arrRec = ProgramRun.fixedRecords(run.arryfile(), ArrArrayRec.LENGTH).get(0);
        assertEquals(zonedNegative, Hex.of(occurrenceZoned(arrRec, 0)), "ARR-ACCT-CURR-BAL(1)");
        assertEquals(zonedNegative, Hex.of(occurrenceZoned(arrRec, 1)), "ARR-ACCT-CURR-BAL(2)");

        byte[] vb2 = ProgramRun.variableRecords(run.vbrcfile()).get(1);
        assertEquals(zonedNegative, Hex.of(Arrays.copyOfRange(vb2, 11, 23)), "VB2-ACCT-CURR-BAL");
        assertEquals(negative, Vb2Rec.fromBytes(vb2, CobolCharset.EBCDIC).currBal());
    }

    private static byte[] occurrenceZoned(byte[] arryRecord, int index) {
        int off = 11 + index * ArrArrayRec.ArrAcctBal.LENGTH;
        return Arrays.copyOfRange(arryRecord, off, off + 12);
    }

    private static byte[] occurrencePacked(byte[] arryRecord, int index) {
        int off = 11 + index * ArrArrayRec.ArrAcctBal.LENGTH + 12;
        return Arrays.copyOfRange(arryRecord, off, off + 7);
    }
}
