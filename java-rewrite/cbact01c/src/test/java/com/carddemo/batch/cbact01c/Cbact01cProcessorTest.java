package com.carddemo.batch.cbact01c;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 tests verifying functional equivalence with CBACT01C.cbl.
 */
class Cbact01cProcessorTest {

    private static AccountRecord createTestRecord(
            long acctId, String status, BigDecimal bal, BigDecimal creditLimit,
            BigDecimal cashCreditLimit, String openDate, String expDate,
            String reissueDate, BigDecimal cycCredit, BigDecimal cycDebit,
            String zip, String groupId) {
        return new AccountRecord(acctId, status, bal, creditLimit, cashCreditLimit,
                openDate, expDate, reissueDate, cycCredit, cycDebit, zip, groupId);
    }

    private static AccountRecord defaultRecord() {
        return createTestRecord(
                12345678901L, "Y",
                new BigDecimal("5000.50"), new BigDecimal("10000.00"),
                new BigDecimal("3000.00"), "2020-01-15", "2025-12-31",
                "2024-06-15", new BigDecimal("1500.00"), new BigDecimal("750.25"),
                "90210", "GRP001"
        );
    }

    /**
     * Test 1: Single record, debit is zero — Verify OUT record gets 2525.00 for currCycDebit.
     */
    @Test
    void testDebitIsZero_substitutedWith2525() {
        AccountRecord acct = createTestRecord(
                12345678901L, "Y",
                new BigDecimal("5000.00"), new BigDecimal("10000.00"),
                new BigDecimal("3000.00"), "2020-01-15", "2025-12-31",
                "2024-06-15", new BigDecimal("1500.00"), BigDecimal.ZERO,
                "90210", "GRP001"
        );

        Cbact01cProcessor processor = new Cbact01cProcessor();
        processor.process(List.of(acct));

        OutAccountRecord out = processor.getOutRecords().get(0);
        assertEquals(0, new BigDecimal("2525.00").compareTo(out.currCycDebit()),
                "When debit is zero, output should be 2525.00");
    }

    /**
     * Test 2: Single record, debit is nonzero — Verify OUT record gets the actual debit value.
     */
    @Test
    void testDebitNonZero_actualValueUsed() {
        AccountRecord acct = defaultRecord();

        Cbact01cProcessor processor = new Cbact01cProcessor();
        processor.process(List.of(acct));

        OutAccountRecord out = processor.getOutRecords().get(0);
        assertEquals(0, new BigDecimal("750.25").compareTo(out.currCycDebit()),
                "When debit is nonzero, output should be the actual value (clean Java behavior)");
    }

    /**
     * Test 3: Date reformatting — Input "2024-06-15" → output "20240615".
     */
    @Test
    void testDateReformatting() {
        assertEquals("20240615", DateFormatter.formatDate("2024-06-15"));
        assertEquals("20000101", DateFormatter.formatDate("2000-01-01"));
        assertEquals("", DateFormatter.formatDate(null));
    }

    /**
     * Test 4: Array record population — Verify 5 slots are populated correctly.
     */
    @Test
    void testArrayRecordPopulation() {
        AccountRecord acct = createTestRecord(
                99999999999L, "A",
                new BigDecimal("7500.75"), new BigDecimal("15000.00"),
                new BigDecimal("5000.00"), "2019-03-20", "2026-03-20",
                "2025-03-17", new BigDecimal("2000.00"), new BigDecimal("500.00"),
                "10001", "GRP002"
        );

        Cbact01cProcessor processor = new Cbact01cProcessor();
        processor.process(List.of(acct));

        ArrayRecord arr = processor.getArrayRecords().get(0);
        assertEquals(99999999999L, arr.acctId());
        assertEquals(5, arr.entries().size());

        // Slot 0: bal = acctCurrBal, debit = 1005.00
        assertEquals(0, new BigDecimal("7500.75").compareTo(arr.entries().get(0).currBal()));
        assertEquals(0, new BigDecimal("1005.00").compareTo(arr.entries().get(0).currCycDebit()));

        // Slot 1: bal = acctCurrBal, debit = 1525.00
        assertEquals(0, new BigDecimal("7500.75").compareTo(arr.entries().get(1).currBal()));
        assertEquals(0, new BigDecimal("1525.00").compareTo(arr.entries().get(1).currCycDebit()));

        // Slot 2: bal = -1025.00, debit = -2500.00
        assertEquals(0, new BigDecimal("-1025.00").compareTo(arr.entries().get(2).currBal()));
        assertEquals(0, new BigDecimal("-2500.00").compareTo(arr.entries().get(2).currCycDebit()));

        // Slots 3-4: bal = 0, debit = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(arr.entries().get(3).currBal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(arr.entries().get(3).currCycDebit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(arr.entries().get(4).currBal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(arr.entries().get(4).currCycDebit()));
    }

    /**
     * Test 5: VBRC record population — Verify VB1 has acctId + status, VB2 has
     * acctId + balance + creditLimit + 4-char year.
     */
    @Test
    void testVbrcRecordPopulation() {
        AccountRecord acct = createTestRecord(
                11111111111L, "N",
                new BigDecimal("3000.00"), new BigDecimal("8000.00"),
                new BigDecimal("2000.00"), "2021-05-10", "2026-05-10",
                "2025-03-17", new BigDecimal("1000.00"), new BigDecimal("200.00"),
                "30301", "GRP003"
        );

        Cbact01cProcessor processor = new Cbact01cProcessor();
        processor.process(List.of(acct));

        List<Object> vbrcRecords = processor.getVbrcRecords();
        assertEquals(2, vbrcRecords.size());

        VbrcRecord1 vb1 = (VbrcRecord1) vbrcRecords.get(0);
        assertEquals(11111111111L, vb1.acctId());
        assertEquals("N", vb1.acctActiveStatus());

        VbrcRecord2 vb2 = (VbrcRecord2) vbrcRecords.get(1);
        assertEquals(11111111111L, vb2.acctId());
        assertEquals(0, new BigDecimal("3000.00").compareTo(vb2.currBal()));
        assertEquals(0, new BigDecimal("8000.00").compareTo(vb2.creditLimit()));
        assertEquals("2025", vb2.reissueYear());
    }

    /**
     * Test 6: Multiple records — Process 3 records and verify correct count and ordering.
     */
    @Test
    void testMultipleRecords() {
        AccountRecord acct1 = createTestRecord(
                10000000001L, "Y", new BigDecimal("1000.00"), new BigDecimal("5000.00"),
                new BigDecimal("2000.00"), "2020-01-01", "2025-01-01",
                "2024-01-01", new BigDecimal("100.00"), new BigDecimal("50.00"),
                "11111", "GRP_A"
        );
        AccountRecord acct2 = createTestRecord(
                20000000002L, "N", new BigDecimal("2000.00"), new BigDecimal("6000.00"),
                new BigDecimal("2500.00"), "2021-02-02", "2026-02-02",
                "2025-02-02", new BigDecimal("200.00"), BigDecimal.ZERO,
                "22222", "GRP_B"
        );
        AccountRecord acct3 = createTestRecord(
                30000000003L, "Y", new BigDecimal("3000.00"), new BigDecimal("7000.00"),
                new BigDecimal("3500.00"), "2022-03-03", "2027-03-03",
                "2026-03-03", new BigDecimal("300.00"), new BigDecimal("150.00"),
                "33333", "GRP_C"
        );

        Cbact01cProcessor processor = new Cbact01cProcessor();
        processor.process(List.of(acct1, acct2, acct3));

        assertEquals(3, processor.getOutRecords().size());
        assertEquals(3, processor.getArrayRecords().size());
        // VBRC produces 2 records per input
        assertEquals(6, processor.getVbrcRecords().size());

        // Verify ordering
        assertEquals(10000000001L, processor.getOutRecords().get(0).acctId());
        assertEquals(20000000002L, processor.getOutRecords().get(1).acctId());
        assertEquals(30000000003L, processor.getOutRecords().get(2).acctId());

        // Verify second record gets debit substitution
        assertEquals(0, new BigDecimal("2525.00").compareTo(
                processor.getOutRecords().get(1).currCycDebit()));
    }

    /**
     * Test 7: Empty input file — Verify no output records are produced and no errors thrown.
     */
    @Test
    void testEmptyInput() {
        Cbact01cProcessor processor = new Cbact01cProcessor();
        processor.process(Collections.emptyList());

        assertTrue(processor.getOutRecords().isEmpty());
        assertTrue(processor.getArrayRecords().isEmpty());
        assertTrue(processor.getVbrcRecords().isEmpty());
    }

    /**
     * Test 8: Reissue year extraction — For reissue date "2025-03-17", verify VB2 reissue year is "2025".
     */
    @Test
    void testReissueYearExtraction() {
        AccountRecord acct = createTestRecord(
                55555555555L, "Y",
                new BigDecimal("4000.00"), new BigDecimal("9000.00"),
                new BigDecimal("4500.00"), "2018-07-20", "2028-07-20",
                "2025-03-17", new BigDecimal("800.00"), new BigDecimal("400.00"),
                "60601", "GRP004"
        );

        Cbact01cProcessor processor = new Cbact01cProcessor();
        processor.process(List.of(acct));

        VbrcRecord2 vb2 = (VbrcRecord2) processor.getVbrcRecords().get(1);
        assertEquals("2025", vb2.reissueYear());
    }

    /**
     * Test 9: Negative balances — Test with negative acctCurrBal to ensure signed decimal handling.
     */
    @Test
    void testNegativeBalances() {
        AccountRecord acct = createTestRecord(
                77777777777L, "Y",
                new BigDecimal("-2500.50"), new BigDecimal("10000.00"),
                new BigDecimal("5000.00"), "2019-11-01", "2024-11-01",
                "2023-11-01", new BigDecimal("-100.25"), new BigDecimal("-300.75"),
                "40404", "GRP005"
        );

        Cbact01cProcessor processor = new Cbact01cProcessor();
        processor.process(List.of(acct));

        OutAccountRecord out = processor.getOutRecords().get(0);
        assertEquals(0, new BigDecimal("-2500.50").compareTo(out.currBal()));
        assertEquals(0, new BigDecimal("-300.75").compareTo(out.currCycDebit()));
        assertEquals(0, new BigDecimal("-100.25").compareTo(out.currCycCredit()));

        // Array slot 0 should have the negative balance
        ArrayRecord arr = processor.getArrayRecords().get(0);
        assertEquals(0, new BigDecimal("-2500.50").compareTo(arr.entries().get(0).currBal()));
        assertEquals(0, new BigDecimal("-2500.50").compareTo(arr.entries().get(1).currBal()));

        // VB2 should have the negative balance
        VbrcRecord2 vb2 = (VbrcRecord2) processor.getVbrcRecords().get(1);
        assertEquals(0, new BigDecimal("-2500.50").compareTo(vb2.currBal()));
    }

    /**
     * Test 10: End-to-end file I/O — Write AccountRecords to a temp file, run the full processor,
     * read back all three output files, and verify contents.
     */
    @Test
    void testEndToEndFileIO(@TempDir Path tempDir) throws Exception {
        // Write input file
        Path inputFile = tempDir.resolve("acctfile.dat");
        String inputLine = "12345678901|Y|5000.50|10000.00|3000.00|2020-01-15|2025-12-31|2024-06-15|1500.00|0|90210|GRP001";
        Files.writeString(inputFile, inputLine + System.lineSeparator());

        // Read input
        AccountFileReader fileReader = new AccountFileReader();
        List<AccountRecord> accounts;
        try (FileReader fr = new FileReader(inputFile.toFile())) {
            accounts = fileReader.readAll(fr);
        }
        assertEquals(1, accounts.size());

        // Process
        Cbact01cProcessor processor = new Cbact01cProcessor();
        processor.process(accounts);

        // Write output files
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        try (FileWriter fw = new FileWriter(outFile.toFile())) {
            new OutFileWriter().writeAll(processor.getOutRecords(), fw);
        }
        try (FileWriter fw = new FileWriter(arryFile.toFile())) {
            new ArrayFileWriter().writeAll(processor.getArrayRecords(), fw);
        }
        try (FileWriter fw = new FileWriter(vbrcFile.toFile())) {
            new VbrcFileWriter().writeAll(processor.getVbrcRecords(), fw);
        }

        // Read back and verify output file
        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(1, outLines.size());
        assertTrue(outLines.get(0).contains("12345678901"));
        assertTrue(outLines.get(0).contains("2525.00")); // debit was zero
        assertTrue(outLines.get(0).contains("20240615")); // reformatted date

        // Verify array file
        List<String> arryLines = Files.readAllLines(arryFile);
        assertEquals(1, arryLines.size());
        assertTrue(arryLines.get(0).contains("12345678901"));
        assertTrue(arryLines.get(0).contains("5000.50")); // acctCurrBal in slots 0,1
        assertTrue(arryLines.get(0).contains("1005.00")); // slot 0 debit
        assertTrue(arryLines.get(0).contains("-1025.00")); // slot 2 bal

        // Verify VBRC file (2 records per input)
        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(2, vbrcLines.size());
        // VB1: acctId + status
        assertTrue(vbrcLines.get(0).contains("12345678901"));
        assertTrue(vbrcLines.get(0).contains("Y"));
        // VB2: acctId + bal + creditLimit + year
        assertTrue(vbrcLines.get(1).contains("12345678901"));
        assertTrue(vbrcLines.get(1).contains("5000.50"));
        assertTrue(vbrcLines.get(1).contains("10000.00"));
        assertTrue(vbrcLines.get(1).contains("2024"));
    }
}
