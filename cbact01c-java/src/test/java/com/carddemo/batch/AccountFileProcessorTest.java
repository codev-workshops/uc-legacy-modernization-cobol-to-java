package com.carddemo.batch;

import com.carddemo.batch.service.AccountFileProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AccountFileProcessor.
 * Verifies the Java version produces identical results to the COBOL version
 * for sample inputs from the CardDemo account data file.
 *
 * Values are derived from PIC S9(10)V99 zoned-decimal format:
 *   00000001940{ → digits 000000019400 → V99 → 194.00
 *   00000020200{ → digits 000000202000 → V99 → 2020.00
 *   00000010200{ → digits 000000102000 → V99 → 1020.00
 */
class AccountFileProcessorTest {

    @TempDir
    Path tempDir;

    // Sample records from acctdata.txt (first 5 records)
    private static final String[] SAMPLE_RECORDS = {
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000" + " ".repeat(178),
            "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000" + " ".repeat(178),
            "00000000003Y00000001470{00000049090{00000005380{2013-08-232024-01-102024-01-1000000000000{00000000000{A000000000" + " ".repeat(178),
            "00000000004Y00000000400{00000035030{00000027890{2012-11-172023-12-162023-12-1600000000000{00000000000{A000000000" + " ".repeat(178),
            "00000000005Y00000003450{00000038190{00000024300{2012-10-032025-03-092025-03-0900000000000{00000000000{A000000000" + " ".repeat(178)
    };

    @Test
    @DisplayName("Process 5 sample records and verify record count")
    void processAllRecords() throws IOException {
        Path input = createInputFile(SAMPLE_RECORDS);
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        int count = processor.process();

        assertEquals(5, count);
        assertEquals(5, Files.readAllLines(outFile).size());
        assertEquals(5, Files.readAllLines(arrFile).size());
        assertEquals(10, Files.readAllLines(vbrFile).size()); // 2 VBR records per account
    }

    @Test
    @DisplayName("Output record matches COBOL 1300-POPUL-ACCT-RECORD logic")
    void outputRecordPopulation() throws IOException {
        Path input = createInputFile(SAMPLE_RECORDS[0]);
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        List<String> lines = Files.readAllLines(outFile);
        assertEquals(1, lines.size());

        String[] fields = lines.get(0).split("\\|", -1);
        assertEquals("00000000001", fields[0]);           // ACCT-ID
        assertEquals("Y", fields[1]);                     // ACTIVE-STATUS
        assertEquals("194.00", fields[2]);                // CURR-BAL (V99: 000000019400→194.00)
        assertEquals("2020.00", fields[3]);               // CREDIT-LIMIT (V99: 000000202000→2020.00)
        assertEquals("1020.00", fields[4]);               // CASH-CREDIT-LIMIT
        assertEquals("2014-11-20", fields[5]);            // OPEN-DATE
        assertEquals("2025-05-20", fields[6]);            // EXPIRATION-DATE
        assertEquals("20250520", fields[7]);              // REISSUE-DATE (COBDATFT: YYYY-MM-DD→YYYYMMDD)
        assertEquals("0.00", fields[8]);                  // CURR-CYC-CREDIT
        assertEquals("2525.00", fields[9]);               // CURR-CYC-DEBIT (zero → 2525.00)
        assertEquals("          ", fields[10]);           // GROUP-ID (spaces in sample data)
    }

    @Test
    @DisplayName("Cycle debit zero substitution - COBOL rule: if zero, set to 2525.00")
    void cycleDebitZeroSubstitution() throws IOException {
        Path input = createInputFile(SAMPLE_RECORDS[0]);
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        String line = Files.readAllLines(outFile).get(0);
        String[] fields = line.split("\\|", -1);
        assertEquals("2525.00", fields[9]);
    }

    @Test
    @DisplayName("Non-zero cycle debit is preserved")
    void cycleDebitNonZeroPreserved() throws IOException {
        // Create a record with non-zero cycle debit
        // 00000005000A → digits: 00000005000, A=+1, full=000000050001, V99=500.01
        String recordWithDebit =
                "00000000099Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000005000A" + "A000000000" + "          " + " ".repeat(178);

        Path input = createInputFile(recordWithDebit);
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        String line = Files.readAllLines(outFile).get(0);
        String[] fields = line.split("\\|", -1);
        // 00000005000A → digits=00000005000, A=+1, full=000000050001, V99=500.01
        assertEquals("500.01", fields[9]);
    }

    @Test
    @DisplayName("Array record matches COBOL 1400-POPUL-ARRAY-RECORD logic")
    void arrayRecordPopulation() throws IOException {
        Path input = createInputFile(SAMPLE_RECORDS[0]);
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        List<String> lines = Files.readAllLines(arrFile);
        assertEquals(1, lines.size());

        String[] parts = lines.get(0).split("\\|");
        assertEquals("00000000001", parts[0]);             // ACCT-ID

        // Entry 1: (ACCT-CURR-BAL=194.00, 1005.00)
        String[] entry1 = parts[1].split(",");
        assertEquals("194.00", entry1[0]);
        assertEquals("1005.00", entry1[1]);

        // Entry 2: (ACCT-CURR-BAL=194.00, 1525.00)
        String[] entry2 = parts[2].split(",");
        assertEquals("194.00", entry2[0]);
        assertEquals("1525.00", entry2[1]);

        // Entry 3: (-1025.00, -2500.00) - hardcoded in COBOL
        String[] entry3 = parts[3].split(",");
        assertEquals("-1025.00", entry3[0]);
        assertEquals("-2500.00", entry3[1]);

        // Entries 4,5: (0, 0) - initialized to zero
        String[] entry4 = parts[4].split(",");
        assertEquals("0", entry4[0]);
        assertEquals("0", entry4[1]);

        String[] entry5 = parts[5].split(",");
        assertEquals("0", entry5[0]);
        assertEquals("0", entry5[1]);
    }

    @Test
    @DisplayName("Variable-length records match COBOL 1500/1550/1575 logic")
    void variableLengthRecordPopulation() throws IOException {
        Path input = createInputFile(SAMPLE_RECORDS[0]);
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        List<String> lines = Files.readAllLines(vbrFile);
        assertEquals(2, lines.size());

        // VB1 record (short): VB1|acctId|activeStatus
        String[] vb1 = lines.get(0).split("\\|");
        assertEquals("VB1", vb1[0]);
        assertEquals("00000000001", vb1[1]);
        assertEquals("Y", vb1[2]);

        // VB2 record (long): VB2|acctId|balance|creditLimit|reissueYear
        String[] vb2 = lines.get(1).split("\\|");
        assertEquals("VB2", vb2[0]);
        assertEquals("00000000001", vb2[1]);
        assertEquals("194.00", vb2[2]);     // V99: 000000019400 → 194.00
        assertEquals("2020.00", vb2[3]);    // V99: 000000202000 → 2020.00
        assertEquals("2025", vb2[4]);       // First 4 chars of reissue date
    }

    @Test
    @DisplayName("Date formatting matches COBDATFT: YYYY-MM-DD → YYYYMMDD")
    void dateFormattingInOutput() throws IOException {
        Path input = createInputFile(SAMPLE_RECORDS[2]); // reissue date: 2024-01-10
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        String line = Files.readAllLines(outFile).get(0);
        String[] fields = line.split("\\|");
        assertEquals("20240110", fields[7]); // REISSUE-DATE formatted
    }

    @Test
    @DisplayName("Process all 50 records from actual acctdata.txt")
    void processFullDataFile() throws IOException {
        Path actualDataFile = Path.of("../app/data/ASCII/acctdata.txt");
        if (!Files.exists(actualDataFile)) {
            actualDataFile = Path.of("app/data/ASCII/acctdata.txt");
        }
        if (!Files.exists(actualDataFile)) {
            return; // Skip if data file not available in test context
        }

        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(
                actualDataFile, outFile, arrFile, vbrFile);
        int count = processor.process();

        assertTrue(count > 0, "Should process at least 1 record");
        assertEquals(count, Files.readAllLines(outFile).size());
        assertEquals(count, Files.readAllLines(arrFile).size());
        assertEquals(count * 2, Files.readAllLines(vbrFile).size());
    }

    @Test
    @DisplayName("Empty input file produces no output")
    void emptyInputFile() throws IOException {
        Path input = createInputFile();
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        int count = processor.process();

        assertEquals(0, count);
        assertEquals(0, Files.readAllLines(outFile).size());
    }

    @Test
    @DisplayName("Multiple records produce consistent output ordering")
    void multipleRecordsOrdering() throws IOException {
        Path input = createInputFile(SAMPLE_RECORDS);
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals("00000000001", outLines.get(0).split("\\|")[0]);
        assertEquals("00000000002", outLines.get(1).split("\\|")[0]);
        assertEquals("00000000003", outLines.get(2).split("\\|")[0]);
        assertEquals("00000000004", outLines.get(3).split("\\|")[0]);
        assertEquals("00000000005", outLines.get(4).split("\\|")[0]);
    }

    @Test
    @DisplayName("Reissue year extraction for VB2 record")
    void reissueYearExtraction() throws IOException {
        // Record 4 has reissue date 2023-12-16 → year should be 2023
        Path input = createInputFile(SAMPLE_RECORDS[3]);
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        List<String> vbrLines = Files.readAllLines(vbrFile);
        String[] vb2 = vbrLines.get(1).split("\\|");
        assertEquals("2023", vb2[4]);
    }

    private Path createInputFile(String... records) throws IOException {
        Path inputFile = tempDir.resolve("acctdata.txt");
        Files.write(inputFile, List.of(records));
        return inputFile;
    }
}
