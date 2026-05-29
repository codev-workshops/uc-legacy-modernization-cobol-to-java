package com.carddemo.harness;

import com.carddemo.harness.parser.DataFileParser;
import com.carddemo.harness.parser.RecordLayout;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the DataFileParser against the actual ASCII data files in app/data/ASCII/.
 */
class DataFileParserTest {

    private static final Path DATA_DIR = findDataDir();

    private static Path findDataDir() {
        // Search upward from working directory for app/data/ASCII
        Path dir = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 5; i++) {
            Path candidate = dir.resolve("app/data/ASCII");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
            if (dir == null) break;
        }
        return Paths.get("../app/data/ASCII");
    }

    @Test
    void parseAcctData() throws IOException {
        Path file = DATA_DIR.resolve("acctdata.txt");
        if (!Files.exists(file)) return;

        DataFileParser parser = new DataFileParser(RecordLayout.accountRecordLayout());
        List<Map<String, Object>> records = parser.parseFile(file);

        assertEquals(50, records.size(), "acctdata should have 50 records");

        Map<String, Object> first = records.get(0);
        assertEquals("00000000001", DataFileParser.fieldValue(first, "ACCT-ID"));
        assertEquals("Y", DataFileParser.fieldValue(first, "ACCT-ACTIVE-STATUS"));
        assertEquals("2014-11-20", DataFileParser.fieldValue(first, "ACCT-OPEN-DATE"));
    }

    @Test
    void parseCardData() throws IOException {
        Path file = DATA_DIR.resolve("carddata.txt");
        if (!Files.exists(file)) return;

        DataFileParser parser = new DataFileParser(RecordLayout.cardRecordLayout());
        List<Map<String, Object>> records = parser.parseFile(file);

        assertEquals(50, records.size(), "carddata should have 50 records");

        Map<String, Object> first = records.get(0);
        assertFalse(DataFileParser.fieldValue(first, "CARD-NUM").isBlank());
        assertFalse(DataFileParser.fieldValue(first, "CARD-ACCT-ID").isBlank());
    }

    @Test
    void parseCardXref() throws IOException {
        Path file = DATA_DIR.resolve("cardxref.txt");
        if (!Files.exists(file)) return;

        DataFileParser parser = new DataFileParser(RecordLayout.cardXrefLayout());
        List<Map<String, Object>> records = parser.parseFile(file);

        assertEquals(50, records.size(), "cardxref should have 50 records");

        Map<String, Object> first = records.get(0);
        assertEquals(16, DataFileParser.fieldValue(first, "XREF-CARD-NUM").length());
    }

    @Test
    void parseCustData() throws IOException {
        Path file = DATA_DIR.resolve("custdata.txt");
        if (!Files.exists(file)) return;

        DataFileParser parser = new DataFileParser(RecordLayout.customerRecordLayout());
        List<Map<String, Object>> records = parser.parseFile(file);

        assertEquals(50, records.size(), "custdata should have 50 records");

        Map<String, Object> first = records.get(0);
        assertEquals("000000001", DataFileParser.fieldValue(first, "CUST-ID"));
        assertFalse(DataFileParser.fieldValue(first, "CUST-FIRST-NAME").isBlank());
    }

    @Test
    void parseDailyTran() throws IOException {
        Path file = DATA_DIR.resolve("dailytran.txt");
        if (!Files.exists(file)) return;

        DataFileParser parser = new DataFileParser(RecordLayout.dailyTranLayout());
        List<Map<String, Object>> records = parser.parseFile(file);

        assertEquals(300, records.size(), "dailytran should have 300 records");
    }

    @Test
    void parseTranType() throws IOException {
        Path file = DATA_DIR.resolve("trantype.txt");
        if (!Files.exists(file)) return;

        DataFileParser parser = new DataFileParser(RecordLayout.tranTypeLayout());
        List<Map<String, Object>> records = parser.parseFile(file);

        assertEquals(7, records.size(), "trantype should have 7 records");
        assertEquals("01", DataFileParser.fieldValue(records.get(0), "TRAN-TYPE"));
    }

    @Test
    void parseTranCatg() throws IOException {
        Path file = DATA_DIR.resolve("trancatg.txt");
        if (!Files.exists(file)) return;

        DataFileParser parser = new DataFileParser(RecordLayout.tranCatLayout());
        List<Map<String, Object>> records = parser.parseFile(file);

        assertEquals(18, records.size(), "trancatg should have 18 records");
    }

    @Test
    void parseTcatBal() throws IOException {
        Path file = DATA_DIR.resolve("tcatbal.txt");
        if (!Files.exists(file)) return;

        DataFileParser parser = new DataFileParser(RecordLayout.tranCatBalLayout());
        List<Map<String, Object>> records = parser.parseFile(file);

        assertEquals(50, records.size(), "tcatbal should have 50 records");

        Map<String, Object> first = records.get(0);
        assertEquals("00000000001", DataFileParser.fieldValue(first, "TRANCAT-ACCT-ID"));
    }

    @Test
    void parseDiscGrp() throws IOException {
        Path file = DATA_DIR.resolve("discgrp.txt");
        if (!Files.exists(file)) return;

        DataFileParser parser = new DataFileParser(RecordLayout.discGroupLayout());
        List<Map<String, Object>> records = parser.parseFile(file);

        assertTrue(records.size() >= 50, "discgrp should have at least 50 records");
    }
}
