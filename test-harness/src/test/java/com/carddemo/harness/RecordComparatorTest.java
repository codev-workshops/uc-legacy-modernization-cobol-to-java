package com.carddemo.harness;

import com.carddemo.harness.codec.PackedDecimalCodec;
import com.carddemo.harness.codec.ZonedDecimalCodec;
import com.carddemo.harness.comparator.RecordComparator;
import com.carddemo.harness.config.ToleranceConfig;
import com.carddemo.harness.parser.RecordLayout;
import com.carddemo.harness.report.ComparisonReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RecordComparatorTest {

    private RecordComparator comparator;
    private ToleranceConfig config;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        comparator = new RecordComparator();
        config = new ToleranceConfig();
    }

    @Test
    void identicalOutfileRecordsAllMatch() throws Exception {
        RecordLayout layout = RecordLayout.outfileLayout();
        byte[] record = buildSampleOutfileRecord();

        Path cobolFile = tempDir.resolve("OUTFILE_COBOL");
        Path javaFile = tempDir.resolve("OUTFILE_JAVA");

        writeBytes(cobolFile, record);
        writeBytes(javaFile, record);

        ComparisonReport report = comparator.compareFixedLength(
                cobolFile, javaFile, layout, "CBACT01C", config);

        assertTrue(report.isFullMatch());
        assertEquals(0, report.getTotalMismatches());
        assertEquals(1, report.getRecords().size());

        String output = report.generate();
        assertTrue(output.contains("MATCH"));
        assertFalse(output.contains("[MISMATCH]"));
    }

    @Test
    void multipleIdenticalRecordsAllMatch() throws Exception {
        RecordLayout layout = RecordLayout.outfileLayout();
        byte[] record = buildSampleOutfileRecord();

        // Write 3 identical records
        byte[] threeRecords = new byte[record.length * 3];
        System.arraycopy(record, 0, threeRecords, 0, record.length);
        System.arraycopy(record, 0, threeRecords, record.length, record.length);
        System.arraycopy(record, 0, threeRecords, record.length * 2, record.length);

        Path cobolFile = tempDir.resolve("OUTFILE_COBOL");
        Path javaFile = tempDir.resolve("OUTFILE_JAVA");

        writeBytes(cobolFile, threeRecords);
        writeBytes(javaFile, threeRecords);

        ComparisonReport report = comparator.compareFixedLength(
                cobolFile, javaFile, layout, "CBACT01C", config);

        assertTrue(report.isFullMatch());
        assertEquals(3, report.getRecords().size());
    }

    @Test
    void mismatchedRecordCountDetected() throws Exception {
        RecordLayout layout = RecordLayout.outfileLayout();
        byte[] record = buildSampleOutfileRecord();

        byte[] twoRecords = new byte[record.length * 2];
        System.arraycopy(record, 0, twoRecords, 0, record.length);
        System.arraycopy(record, 0, twoRecords, record.length, record.length);

        Path cobolFile = tempDir.resolve("OUTFILE_COBOL");
        Path javaFile = tempDir.resolve("OUTFILE_JAVA");

        writeBytes(cobolFile, twoRecords);
        writeBytes(javaFile, record); // only 1 record

        ComparisonReport report = comparator.compareFixedLength(
                cobolFile, javaFile, layout, "CBACT01C", config);

        assertFalse(report.isFullMatch());
        String output = report.generate();
        assertTrue(output.contains("COBOL=2"));
        assertTrue(output.contains("Java=1"));
    }

    @Test
    void reportFormatContainsExpectedSections() throws Exception {
        RecordLayout layout = RecordLayout.outfileLayout();
        byte[] record = buildSampleOutfileRecord();

        Path cobolFile = tempDir.resolve("OUTFILE_COBOL");
        Path javaFile = tempDir.resolve("OUTFILE_JAVA");
        writeBytes(cobolFile, record);
        writeBytes(javaFile, record);

        ComparisonReport report = comparator.compareFixedLength(
                cobolFile, javaFile, layout, "CBACT01C", config);

        String output = report.generate();
        assertTrue(output.contains("=== CBACT01C Output Comparison Report ==="));
        assertTrue(output.contains("File: OUTFILE"));
        assertTrue(output.contains("Record 001:"));
        assertTrue(output.contains("Summary:"));
    }

    @Test
    void accountRecordLayoutMatchesExpectedLength() {
        RecordLayout layout = RecordLayout.accountRecordLayout();
        assertEquals(300, layout.getRecordLength());
    }

    @Test
    void tranRecordLayoutMatchesExpectedLength() {
        RecordLayout layout = RecordLayout.tranRecordLayout();
        assertEquals(350, layout.getRecordLength());
    }

    @Test
    void arryFileLayoutMatchesExpectedLength() {
        RecordLayout layout = RecordLayout.arryFileLayout();
        assertEquals(110, layout.getRecordLength());
    }

    @Test
    void toleranceAwareComparisonZeroToleranceMismatch() throws Exception {
        RecordLayout layout = RecordLayout.outfileLayout();
        byte[] record1 = buildSampleOutfileRecord();
        byte[] record2 = buildSampleOutfileRecord();

        // Modify OUT-ACCT-CURR-BAL in record2: change from 1940.00 to 1940.01 (+0.01 difference)
        byte[] modifiedBal = ZonedDecimalCodec.encode(new BigDecimal("1940.01"), 12, 2);
        System.arraycopy(modifiedBal, 0, record2, 12, 12);

        Path cobolFile = tempDir.resolve("OUTFILE_TOL_COBOL");
        Path javaFile = tempDir.resolve("OUTFILE_TOL_JAVA");

        writeBytes(cobolFile, record1);
        writeBytes(javaFile, record2);

        // With default (ZERO) tolerance, should report a mismatch
        ComparisonReport report = comparator.compareFixedLength(
                cobolFile, javaFile, layout, "CBACT01C", config);
        assertFalse(report.isFullMatch(), "Zero tolerance should detect 0.01 difference");
        assertTrue(report.getTotalMismatches() > 0);
    }

    @Test
    void toleranceAwareComparisonGlobalTolerancePass() throws Exception {
        RecordLayout layout = RecordLayout.outfileLayout();
        byte[] record1 = buildSampleOutfileRecord();
        byte[] record2 = buildSampleOutfileRecord();

        // Modify OUT-ACCT-CURR-BAL in record2: +0.01 difference
        byte[] modifiedBal = ZonedDecimalCodec.encode(new BigDecimal("1940.01"), 12, 2);
        System.arraycopy(modifiedBal, 0, record2, 12, 12);

        Path cobolFile = tempDir.resolve("OUTFILE_TOL2_COBOL");
        Path javaFile = tempDir.resolve("OUTFILE_TOL2_JAVA");

        writeBytes(cobolFile, record1);
        writeBytes(javaFile, record2);

        // With global tolerance of 0.01, should pass
        config.setNumericTolerance(new BigDecimal("0.01"));
        ComparisonReport report = comparator.compareFixedLength(
                cobolFile, javaFile, layout, "CBACT01C", config);
        assertTrue(report.isFullMatch(), "0.01 tolerance should allow 0.01 difference");
    }

    @Test
    void toleranceAwareComparisonFieldSpecificOverride() throws Exception {
        RecordLayout layout = RecordLayout.outfileLayout();
        byte[] record1 = buildSampleOutfileRecord();
        byte[] record2 = buildSampleOutfileRecord();

        // Modify OUT-ACCT-CURR-BAL in record2: +0.01 difference
        byte[] modifiedBal = ZonedDecimalCodec.encode(new BigDecimal("1940.01"), 12, 2);
        System.arraycopy(modifiedBal, 0, record2, 12, 12);

        Path cobolFile = tempDir.resolve("OUTFILE_TOL3_COBOL");
        Path javaFile = tempDir.resolve("OUTFILE_TOL3_JAVA");

        writeBytes(cobolFile, record1);
        writeBytes(javaFile, record2);

        // Global tolerance is ZERO, but field-specific tolerance for OUT-ACCT-CURR-BAL is 0.01
        config.setNumericTolerance(BigDecimal.ZERO);
        config.getFieldTolerances().put("OUT-ACCT-CURR-BAL", new BigDecimal("0.01"));

        ComparisonReport report = comparator.compareFixedLength(
                cobolFile, javaFile, layout, "CBACT01C", config);
        assertTrue(report.isFullMatch(),
                "Field-specific tolerance should override global and allow 0.01 difference");
    }

    /**
     * Builds a sample OUTFILE record (107 bytes) for testing.
     */
    private byte[] buildSampleOutfileRecord() {
        byte[] record = new byte[107];

        // OUT-ACCT-ID: 11 bytes, unsigned display → "00000000001"
        byte[] acctId = "00000000001".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(acctId, 0, record, 0, 11);

        // OUT-ACCT-ACTIVE-STATUS: 1 byte
        record[11] = (byte) 'Y';

        // OUT-ACCT-CURR-BAL: 12 bytes, PIC S9(10)V99, value +1940.00
        byte[] currBal = ZonedDecimalCodec.encode(new BigDecimal("1940.00"), 12, 2);
        System.arraycopy(currBal, 0, record, 12, 12);

        // OUT-ACCT-CREDIT-LIMIT: 12 bytes, value +5000.00
        byte[] creditLimit = ZonedDecimalCodec.encode(new BigDecimal("5000.00"), 12, 2);
        System.arraycopy(creditLimit, 0, record, 24, 12);

        // OUT-ACCT-CASH-CREDIT-LIMIT: 12 bytes, value +1000.00
        byte[] cashLimit = ZonedDecimalCodec.encode(new BigDecimal("1000.00"), 12, 2);
        System.arraycopy(cashLimit, 0, record, 36, 12);

        // OUT-ACCT-OPEN-DATE: 10 bytes
        byte[] openDate = "2020-01-15".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(openDate, 0, record, 48, 10);

        // OUT-ACCT-EXPIRAION-DATE: 10 bytes
        byte[] expDate = "2026-12-31".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(expDate, 0, record, 58, 10);

        // OUT-ACCT-REISSUE-DATE: 10 bytes (date field)
        byte[] reissueDate = "20250520  ".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(reissueDate, 0, record, 68, 10);

        // OUT-ACCT-CURR-CYC-CREDIT: 12 bytes, value +500.00
        byte[] cycCredit = ZonedDecimalCodec.encode(new BigDecimal("500.00"), 12, 2);
        System.arraycopy(cycCredit, 0, record, 78, 12);

        // OUT-ACCT-CURR-CYC-DEBIT: 7 bytes COMP-3, value +2525.00
        byte[] cycDebit = PackedDecimalCodec.encode(new BigDecimal("2525.00"), 7, 2);
        System.arraycopy(cycDebit, 0, record, 90, 7);

        // OUT-ACCT-GROUP-ID: 10 bytes
        byte[] groupId = "GROUP001  ".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(groupId, 0, record, 97, 10);

        return record;
    }

    private void writeBytes(Path path, byte[] data) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(data);
        }
    }
}
