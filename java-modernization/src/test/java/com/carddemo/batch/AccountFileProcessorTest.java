package com.carddemo.batch;

import com.carddemo.io.AccountFileReader;
import com.carddemo.model.*;
import com.carddemo.util.CobolDecimalParser;
import com.carddemo.util.DateConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying the Java AccountFileProcessor produces identical results
 * to the COBOL CBACT01C program for a set of sample inputs.
 */
class AccountFileProcessorTest {

    @TempDir
    Path tempDir;

    private Path inputFile;
    private Path outFile;
    private Path arrFile;
    private Path vbrFile;

    @BeforeEach
    void setUp() throws IOException {
        inputFile = tempDir.resolve("acctfile.txt");
        outFile = tempDir.resolve("outfile.txt");
        arrFile = tempDir.resolve("arryfile.txt");
        vbrFile = tempDir.resolve("vbrcfile.txt");
    }

    // -----------------------------------------------------------------------
    // CobolDecimalParser tests
    // -----------------------------------------------------------------------

    @Test
    void parseSignedDecimal_positiveOverpunch() {
        // "00000001940{" → { is +0 → 000000019400 / 100 = 194.00
        BigDecimal result = CobolDecimalParser.parseSignedDecimal("00000001940{", 2);
        assertEquals(new BigDecimal("194.00"), result);
    }

    @Test
    void parseSignedDecimal_negativeOverpunch() {
        // "0000000102N" → N is -5 → -(00000001025) / 100 = -10.25
        BigDecimal result = CobolDecimalParser.parseSignedDecimal("0000000102N", 2);
        assertEquals(new BigDecimal("-10.25"), result);
    }

    @Test
    void parseSignedDecimal_allZeros() {
        BigDecimal result = CobolDecimalParser.parseSignedDecimal("00000000000{", 2);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void formatSignedDecimal_positive() {
        String result = CobolDecimalParser.formatSignedDecimal(new BigDecimal("194.00"), 12, 2);
        assertEquals("00000001940{", result);
    }

    @Test
    void formatSignedDecimal_negative() {
        String result = CobolDecimalParser.formatSignedDecimal(new BigDecimal("-1025.00"), 12, 2);
        assertEquals("00000010250}", result);
    }

    @Test
    void formatSignedDecimal_zero() {
        String result = CobolDecimalParser.formatSignedDecimal(BigDecimal.ZERO, 12, 2);
        assertEquals("00000000000{", result);
    }

    @Test
    void roundTripConversion() {
        BigDecimal original = new BigDecimal("12345.67");
        String formatted = CobolDecimalParser.formatSignedDecimal(original, 12, 2);
        BigDecimal parsed = CobolDecimalParser.parseSignedDecimal(formatted, 2);
        assertEquals(0, original.compareTo(parsed));
    }

    // -----------------------------------------------------------------------
    // DateConverter tests
    // -----------------------------------------------------------------------

    @Test
    void dateConvert_yyyyMmDd_to_yyyymmdd() {
        // Type 2 (YYYY-MM-DD) → Type 2 (YYYYMMDD)
        String result = DateConverter.convert("2025-05-20", 2, 2);
        assertEquals("20250520", result);
    }

    @Test
    void dateConvert_yyyymmdd_to_yyyyMmDd() {
        // Type 1 (YYYYMMDD) → Type 1 (YYYY-MM-DD)
        String result = DateConverter.convert("20250520", 1, 1);
        assertEquals("2025-05-20", result);
    }

    @Test
    void dateConvert_crossFormat() {
        // Type 1 (YYYYMMDD) → Type 2 (YYYYMMDD) — same output
        String result = DateConverter.convert("20250520", 1, 2);
        assertEquals("20250520", result);
    }

    // -----------------------------------------------------------------------
    // AccountFileReader tests
    // -----------------------------------------------------------------------

    @Test
    void readAccountRecords_parsesCorrectly() throws IOException {
        Path testData = Path.of("src/test/resources/test-acctdata.txt");
        if (!Files.exists(testData)) {
            testData = tempDir.resolve("testacct.txt");
            writeSingleRecordFile(testData);
        }

        try (AccountFileReader reader = new AccountFileReader(testData)) {
            Optional<AccountRecord> opt = reader.readNext();
            assertTrue(opt.isPresent());
            AccountRecord rec = opt.get();
            assertEquals("00000000001", rec.acctId());
            assertEquals("Y", rec.activeStatus());
            assertEquals(new BigDecimal("194.00"), rec.currBal());
            assertEquals(new BigDecimal("2020.00"), rec.creditLimit());
            assertEquals(new BigDecimal("1020.00"), rec.cashCreditLimit());
            assertEquals("2014-11-20", rec.openDate());
            assertEquals("2025-05-20", rec.expirationDate());
            assertEquals("2025-05-20", rec.reissueDate());
            assertEquals(0, rec.currCycCredit().compareTo(BigDecimal.ZERO));
            assertEquals(0, rec.currCycDebit().compareTo(BigDecimal.ZERO));
            assertEquals("          ", rec.groupId());
        }
    }

    // -----------------------------------------------------------------------
    // Business logic tests (buildOutputRecord)
    // -----------------------------------------------------------------------

    @Test
    void buildOutputRecord_zeroCycDebit_replacedWith2525() {
        AccountFileProcessor processor = createProcessor();

        AccountRecord acct = new AccountRecord(
                "00000000001", "Y",
                new BigDecimal("194.00"), new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "A000000000", "A000000000"
        );

        OutputAccountRecord out = processor.buildOutputRecord(acct);

        assertEquals("00000000001", out.acctId());
        assertEquals("Y", out.activeStatus());
        assertEquals(new BigDecimal("194.00"), out.currBal());
        assertEquals(new BigDecimal("2020.00"), out.creditLimit());
        assertEquals(new BigDecimal("1020.00"), out.cashCreditLimit());
        assertEquals("2014-11-20", out.openDate());
        assertEquals("2025-05-20", out.expirationDate());
        // Reissue date converted: YYYY-MM-DD → YYYYMMDD
        assertEquals("20250520", out.reissueDate());
        assertEquals(0, out.currCycCredit().compareTo(BigDecimal.ZERO));
        // Zero debit replaced with 2525.00
        assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
        assertEquals("A000000000", out.groupId());
    }

    @Test
    void buildOutputRecord_nonZeroCycDebit_preserved() {
        AccountFileProcessor processor = createProcessor();

        AccountRecord acct = new AccountRecord(
                "00000000010", "Y",
                new BigDecimal("500.00"), new BigDecimal("1000.00"), new BigDecimal("800.00"),
                "2015-01-01", "2025-12-31", "2024-06-15",
                new BigDecimal("100.00"), new BigDecimal("250.50"),
                "B000000000", "B000000000"
        );

        OutputAccountRecord out = processor.buildOutputRecord(acct);
        // Non-zero debit should be preserved
        assertEquals(new BigDecimal("250.50"), out.currCycDebit());
    }

    // -----------------------------------------------------------------------
    // Business logic tests (buildArrayRecord)
    // -----------------------------------------------------------------------

    @Test
    void buildArrayRecord_correctStructure() {
        AccountFileProcessor processor = createProcessor();

        AccountRecord acct = new AccountRecord(
                "00000000001", "Y",
                new BigDecimal("194.00"), new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "A000000000", "A000000000"
        );

        ArrayRecord arr = processor.buildArrayRecord(acct);

        assertEquals("00000000001", arr.acctId());
        assertEquals(5, arr.balanceEntries().size());

        // Index 1: currBal + 1005.00
        assertEquals(new BigDecimal("194.00"), arr.balanceEntries().get(0).currBal());
        assertEquals(new BigDecimal("1005.00"), arr.balanceEntries().get(0).currCycDebit());

        // Index 2: currBal + 1525.00
        assertEquals(new BigDecimal("194.00"), arr.balanceEntries().get(1).currBal());
        assertEquals(new BigDecimal("1525.00"), arr.balanceEntries().get(1).currCycDebit());

        // Index 3: -1025.00 + -2500.00
        assertEquals(new BigDecimal("-1025.00"), arr.balanceEntries().get(2).currBal());
        assertEquals(new BigDecimal("-2500.00"), arr.balanceEntries().get(2).currCycDebit());

        // Indices 4-5: zeros
        assertEquals(0, arr.balanceEntries().get(3).currBal().compareTo(BigDecimal.ZERO));
        assertEquals(0, arr.balanceEntries().get(3).currCycDebit().compareTo(BigDecimal.ZERO));
        assertEquals(0, arr.balanceEntries().get(4).currBal().compareTo(BigDecimal.ZERO));
        assertEquals(0, arr.balanceEntries().get(4).currCycDebit().compareTo(BigDecimal.ZERO));
    }

    // -----------------------------------------------------------------------
    // Business logic tests (buildVbRecords)
    // -----------------------------------------------------------------------

    @Test
    void buildVbRecord1_correctFields() {
        AccountFileProcessor processor = createProcessor();

        AccountRecord acct = sampleAccount();
        VariableLengthRecord1 vb1 = processor.buildVbRecord1(acct);

        assertEquals("00000000001", vb1.acctId());
        assertEquals("Y", vb1.activeStatus());
    }

    @Test
    void buildVbRecord2_correctFields() {
        AccountFileProcessor processor = createProcessor();

        AccountRecord acct = sampleAccount();
        VariableLengthRecord2 vb2 = processor.buildVbRecord2(acct);

        assertEquals("00000000001", vb2.acctId());
        assertEquals(new BigDecimal("194.00"), vb2.currBal());
        assertEquals(new BigDecimal("2020.00"), vb2.creditLimit());
        assertEquals("2025", vb2.reissueYear());
    }

    // -----------------------------------------------------------------------
    // End-to-end integration test
    // -----------------------------------------------------------------------

    @Test
    void endToEnd_processesAllRecords() throws IOException {
        writeSampleInputFile(inputFile);

        AccountFileProcessor processor = new AccountFileProcessor(
                inputFile, outFile, arrFile, vbrFile
        );
        processor.process();

        // Verify output files exist and have correct line counts
        List<String> outLines = Files.readAllLines(outFile);
        List<String> arrLines = Files.readAllLines(arrFile);
        List<String> vbrLines = Files.readAllLines(vbrFile);

        assertEquals(3, outLines.size(), "OUT file should have 3 records");
        assertEquals(3, arrLines.size(), "ARRY file should have 3 records");
        assertEquals(6, vbrLines.size(), "VBR file should have 6 records (2 per account)");
    }

    @Test
    void endToEnd_outputFileContents_matchCobolBehavior() throws IOException {
        writeSampleInputFile(inputFile);

        AccountFileProcessor processor = new AccountFileProcessor(
                inputFile, outFile, arrFile, vbrFile
        );
        processor.process();

        List<String> outLines = Files.readAllLines(outFile);
        String firstOut = outLines.get(0);

        // Verify first output record structure
        // Account ID (11) + Status (1) + CurrBal (12) + CreditLimit (12)
        // + CashCreditLimit (12) + OpenDate (10) + ExpDate (10) + ReissueDate (10)
        // + CycCredit (12) + CycDebit (12) + GroupId (10) = 122 chars
        assertTrue(firstOut.startsWith("00000000001Y"));

        // Verify reissue date was converted from YYYY-MM-DD to YYYYMMDD
        // Position: 11+1+12+12+12+10+10 = 68, length 10
        String reissueInOutput = firstOut.substring(68, 78);
        assertTrue(reissueInOutput.startsWith("20250520"),
                "Reissue date should be YYYYMMDD format, got: " + reissueInOutput);

        // Verify zero debit was replaced with 2525.00
        // CycDebit position: 68+10+12 = 90, length 12
        String cycDebitStr = firstOut.substring(90, 102);
        BigDecimal cycDebit = CobolDecimalParser.parseSignedDecimal(cycDebitStr, 2);
        assertEquals(new BigDecimal("2525.00"), cycDebit,
                "Zero CYC-DEBIT should be replaced with 2525.00");
    }

    @Test
    void endToEnd_arrayFileContents_matchCobolBehavior() throws IOException {
        writeSampleInputFile(inputFile);

        AccountFileProcessor processor = new AccountFileProcessor(
                inputFile, outFile, arrFile, vbrFile
        );
        processor.process();

        List<String> arrLines = Files.readAllLines(arrFile);
        String firstArr = arrLines.get(0);

        // Verify account ID
        assertTrue(firstArr.startsWith("00000000001"));

        // Verify array entry 3 has -1025.00 balance
        // Position: 11 + 2*(12+12) = 59, length 12
        String bal3Str = firstArr.substring(59, 71);
        BigDecimal bal3 = CobolDecimalParser.parseSignedDecimal(bal3Str, 2);
        assertEquals(new BigDecimal("-1025.00"), bal3,
                "Array index 3 balance should be -1025.00");

        // Verify array entry 3 has -2500.00 debit
        String debit3Str = firstArr.substring(71, 83);
        BigDecimal debit3 = CobolDecimalParser.parseSignedDecimal(debit3Str, 2);
        assertEquals(new BigDecimal("-2500.00"), debit3,
                "Array index 3 debit should be -2500.00");
    }

    @Test
    void endToEnd_vbrFileContents_matchCobolBehavior() throws IOException {
        writeSampleInputFile(inputFile);

        AccountFileProcessor processor = new AccountFileProcessor(
                inputFile, outFile, arrFile, vbrFile
        );
        processor.process();

        List<String> vbrLines = Files.readAllLines(vbrFile);

        // VB1 record for first account
        String vb1Line = vbrLines.get(0);
        assertEquals("00000000001Y", vb1Line, "VB1 should be acctId + status");

        // VB2 record for first account
        String vb2Line = vbrLines.get(1);
        assertTrue(vb2Line.startsWith("00000000001"));
        assertEquals(39, vb2Line.length(), "VB2 record should be 39 chars");

        // Verify reissue year in VB2
        String reissueYear = vb2Line.substring(35, 39);
        assertEquals("2025", reissueYear, "VB2 reissue year should match input");
    }

    @Test
    void endToEnd_emptyInputFile_producesEmptyOutputs() throws IOException {
        Files.writeString(inputFile, "");

        AccountFileProcessor processor = new AccountFileProcessor(
                inputFile, outFile, arrFile, vbrFile
        );
        processor.process();

        assertEquals(0, Files.readAllLines(outFile).size());
        assertEquals(0, Files.readAllLines(arrFile).size());
        assertEquals(0, Files.readAllLines(vbrFile).size());
    }

    @Test
    void endToEnd_nonZeroCycDebit_preserved() throws IOException {
        // Create a record where CYC-DEBIT is non-zero (e.g. 500.00 → "00000005000{")
        String record = padTo300(
                "00000000099Y"
                        + "00000005000{"  // currBal = 500.00
                        + "00000010000{"  // creditLimit = 1000.00
                        + "00000008000{"  // cashCreditLimit = 800.00
                        + "2015-01-01"
                        + "2025-12-31"
                        + "2024-06-15"
                        + "00000001000{"  // currCycCredit = 100.00
                        + "00000002505{"  // currCycDebit = 250.50
                        + "C000000000"
                        + "C000000000"
        );
        Files.writeString(inputFile, record + "\n");

        AccountFileProcessor processor = new AccountFileProcessor(
                inputFile, outFile, arrFile, vbrFile
        );
        processor.process();

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(1, outLines.size());

        // CycDebit position: 11+1+12+12+12+10+10+10+12 = 90, length 12
        String cycDebitStr = outLines.get(0).substring(90, 102);
        BigDecimal cycDebit = CobolDecimalParser.parseSignedDecimal(cycDebitStr, 2);
        assertEquals(new BigDecimal("250.50"), cycDebit,
                "Non-zero CYC-DEBIT should be preserved as-is");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private AccountFileProcessor createProcessor() {
        return new AccountFileProcessor(inputFile, outFile, arrFile, vbrFile);
    }

    private AccountRecord sampleAccount() {
        return new AccountRecord(
                "00000000001", "Y",
                new BigDecimal("194.00"), new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "A000000000", "A000000000"
        );
    }

    private void writeSampleInputFile(Path path) throws IOException {
        String rec1 = padTo300("00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000");
        String rec2 = padTo300("00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000");
        String rec3 = padTo300("00000000003Y00000001470{00000049090{00000005380{2013-08-232024-01-102024-01-1000000000000{00000000000{A000000000");
        Files.writeString(path, rec1 + "\n" + rec2 + "\n" + rec3 + "\n");
    }

    private void writeSingleRecordFile(Path path) throws IOException {
        String rec = padTo300("00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000");
        Files.writeString(path, rec + "\n");
    }

    private String padTo300(String s) {
        if (s.length() >= 300) return s.substring(0, 300);
        return s + " ".repeat(300 - s.length());
    }
}
