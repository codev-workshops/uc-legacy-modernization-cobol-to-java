package com.carddemo.harness;

import com.carddemo.harness.parser.DataFileParser;
import com.carddemo.harness.parser.RecordLayout;
import com.carddemo.harness.reconciliation.ReconciliationChecker;
import com.carddemo.harness.validation.ValidationResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests cross-file reconciliation checks against the actual ASCII data files.
 */
class ReconciliationCheckerTest {

    private static final Path DATA_DIR = findDataDir();
    private static final ReconciliationChecker checker = new ReconciliationChecker();

    private static List<Map<String, Object>> acctRecords;
    private static List<Map<String, Object>> cardRecords;
    private static List<Map<String, Object>> xrefRecords;
    private static List<Map<String, Object>> custRecords;
    private static List<Map<String, Object>> dailyTranRecords;
    private static List<Map<String, Object>> tcatbalRecords;
    private static List<Map<String, Object>> discGrpRecords;

    private static boolean dataAvailable = false;

    private static Path findDataDir() {
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

    @BeforeAll
    static void loadData() throws IOException {
        if (!Files.isDirectory(DATA_DIR)) {
            return;
        }

        Path acctFile = DATA_DIR.resolve("acctdata.txt");
        Path cardFile = DATA_DIR.resolve("carddata.txt");
        Path xrefFile = DATA_DIR.resolve("cardxref.txt");
        Path custFile = DATA_DIR.resolve("custdata.txt");
        Path tranFile = DATA_DIR.resolve("dailytran.txt");
        Path tcatFile = DATA_DIR.resolve("tcatbal.txt");
        Path discFile = DATA_DIR.resolve("discgrp.txt");

        if (!Files.exists(acctFile) || !Files.exists(cardFile) || !Files.exists(xrefFile)
                || !Files.exists(custFile) || !Files.exists(tranFile)) {
            return;
        }

        acctRecords = new DataFileParser(RecordLayout.accountRecordLayout()).parseFile(acctFile);
        cardRecords = new DataFileParser(RecordLayout.cardRecordLayout()).parseFile(cardFile);
        xrefRecords = new DataFileParser(RecordLayout.cardXrefLayout()).parseFile(xrefFile);
        custRecords = new DataFileParser(RecordLayout.customerRecordLayout()).parseFile(custFile);
        dailyTranRecords = new DataFileParser(RecordLayout.dailyTranLayout()).parseFile(tranFile);
        tcatbalRecords = new DataFileParser(RecordLayout.tranCatBalLayout()).parseFile(tcatFile);
        discGrpRecords = new DataFileParser(RecordLayout.discGroupLayout()).parseFile(discFile);

        dataAvailable = true;
    }

    @Test
    void cardXrefToCardData() {
        if (!dataAvailable) return;
        ValidationResult result = checker.checkCardXrefToCardData(xrefRecords, cardRecords);
        assertTrue(result.isPassed(), "Every XREF card should exist in CARDDATA: " + result.getMessage());
    }

    @Test
    void cardXrefToAcctData() {
        if (!dataAvailable) return;
        ValidationResult result = checker.checkCardXrefToAcctData(xrefRecords, acctRecords);
        assertTrue(result.isPassed(), "Every XREF account should exist in ACCTDATA: " + result.getMessage());
    }

    @Test
    void cardXrefToCustData() {
        if (!dataAvailable) return;
        ValidationResult result = checker.checkCardXrefToCustData(xrefRecords, custRecords);
        assertTrue(result.isPassed(), "Every XREF customer should exist in CUSTDATA: " + result.getMessage());
    }

    @Test
    void dailyTranToCardXref() {
        if (!dataAvailable) return;
        ValidationResult result = checker.checkDailyTranToCardXref(dailyTranRecords, xrefRecords);
        assertTrue(result.isPassed(), "Every daily tran card should exist in XREF: " + result.getMessage());
    }

    @Test
    void tcatbalToAcctData() {
        if (!dataAvailable) return;
        ValidationResult result = checker.checkTcatbalToAcctData(tcatbalRecords, acctRecords);
        assertTrue(result.isPassed(), "Every TCATBAL account should exist in ACCTDATA: " + result.getMessage());
    }

    @Test
    void acctGroupIdsExistInDiscGrp() {
        if (!dataAvailable) return;
        // Check the valid referential direction: accounts referencing a group
        // should find that group in the disclosure group reference table.
        // The disclosure group table may contain extra entries (e.g., DEFAULT, ZEROAPR)
        // not yet assigned to any account.
        Set<String> discGroupIds = new HashSet<>();
        for (Map<String, Object> disc : discGrpRecords) {
            String gid = DataFileParser.fieldValue(disc, "DIS-ACCT-GROUP-ID");
            if (!gid.isEmpty()) discGroupIds.add(gid);
        }
        List<String> orphans = new ArrayList<>();
        for (Map<String, Object> acct : acctRecords) {
            String gid = DataFileParser.fieldValue(acct, "ACCT-GROUP-ID");
            if (!gid.isEmpty() && !discGroupIds.contains(gid)) {
                orphans.add(gid);
            }
        }
        assertTrue(orphans.isEmpty(),
                "Accounts reference group IDs not in DISCGRP: " + orphans);
    }

    @Test
    void acctDataRecordCount() {
        if (!dataAvailable) return;
        ValidationResult result = checker.checkRecordCount(acctRecords, 50, "ACCTDATA");
        assertTrue(result.isPassed(), result.getMessage());
    }

    @Test
    void cardDataRecordCount() {
        if (!dataAvailable) return;
        ValidationResult result = checker.checkRecordCount(cardRecords, 50, "CARDDATA");
        assertTrue(result.isPassed(), result.getMessage());
    }

    @Test
    void custDataRecordCount() {
        if (!dataAvailable) return;
        ValidationResult result = checker.checkRecordCount(custRecords, 50, "CUSTDATA");
        assertTrue(result.isPassed(), result.getMessage());
    }

    @Test
    void dailyTranRecordCount() {
        if (!dataAvailable) return;
        ValidationResult result = checker.checkRecordCount(dailyTranRecords, 300, "DAILYTRAN");
        assertTrue(result.isPassed(), result.getMessage());
    }

    @Test
    void xrefRecordCount() {
        if (!dataAvailable) return;
        ValidationResult result = checker.checkRecordCount(xrefRecords, 50, "CARDXREF");
        assertTrue(result.isPassed(), result.getMessage());
    }
}
