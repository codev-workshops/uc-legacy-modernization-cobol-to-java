package com.carddemo.harness.reconciliation;

import com.carddemo.harness.parser.DataFileParser;
import com.carddemo.harness.validation.ValidationResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cross-file reconciliation checks for CardDemo data integrity.
 *
 * These checks validate referential integrity across the data files:
 * - Every card in cardxref has a matching card in carddata
 * - Every card in cardxref has a matching account in acctdata
 * - Every card in cardxref has a matching customer in custdata
 * - Every daily transaction card number exists in cardxref
 * - Every tcatbal account exists in acctdata
 * - Every disclosure group ID exists as an account group in acctdata
 */
public class ReconciliationChecker {

    /**
     * Validate that every XREF-CARD-NUM in cardxref has a matching CARD-NUM in carddata.
     */
    public ValidationResult checkCardXrefToCardData(
            List<Map<String, Object>> xrefRecords,
            List<Map<String, Object>> cardRecords) {
        Set<String> cardNums = extractFieldSet(cardRecords, "CARD-NUM");
        List<String> orphans = new ArrayList<>();

        for (Map<String, Object> xref : xrefRecords) {
            String xrefCard = DataFileParser.fieldValue(xref, "XREF-CARD-NUM");
            if (!xrefCard.isEmpty() && !cardNums.contains(xrefCard)) {
                orphans.add(xrefCard);
            }
        }

        if (!orphans.isEmpty()) {
            return ValidationResult.fail(
                    "Card XREF has %d card numbers not found in CARDDATA: %s",
                    orphans.size(), orphans.size() <= 5 ? orphans.toString() : orphans.subList(0, 5) + "...");
        }
        return ValidationResult.pass();
    }

    /**
     * Validate that every XREF-ACCT-ID in cardxref has a matching ACCT-ID in acctdata.
     */
    public ValidationResult checkCardXrefToAcctData(
            List<Map<String, Object>> xrefRecords,
            List<Map<String, Object>> acctRecords) {
        Set<String> acctIds = extractFieldSet(acctRecords, "ACCT-ID");
        List<String> orphans = new ArrayList<>();

        for (Map<String, Object> xref : xrefRecords) {
            String xrefAcct = DataFileParser.fieldValue(xref, "XREF-ACCT-ID");
            if (!xrefAcct.isEmpty() && !acctIds.contains(xrefAcct)) {
                orphans.add(xrefAcct);
            }
        }

        if (!orphans.isEmpty()) {
            return ValidationResult.fail(
                    "Card XREF has %d account IDs not found in ACCTDATA: %s",
                    orphans.size(), orphans.size() <= 5 ? orphans.toString() : orphans.subList(0, 5) + "...");
        }
        return ValidationResult.pass();
    }

    /**
     * Validate that every XREF-CUST-ID in cardxref has a matching CUST-ID in custdata.
     */
    public ValidationResult checkCardXrefToCustData(
            List<Map<String, Object>> xrefRecords,
            List<Map<String, Object>> custRecords) {
        Set<String> custIds = extractFieldSet(custRecords, "CUST-ID");
        List<String> orphans = new ArrayList<>();

        for (Map<String, Object> xref : xrefRecords) {
            String xrefCust = DataFileParser.fieldValue(xref, "XREF-CUST-ID");
            if (!xrefCust.isEmpty() && !custIds.contains(xrefCust)) {
                orphans.add(xrefCust);
            }
        }

        if (!orphans.isEmpty()) {
            return ValidationResult.fail(
                    "Card XREF has %d customer IDs not found in CUSTDATA: %s",
                    orphans.size(), orphans.size() <= 5 ? orphans.toString() : orphans.subList(0, 5) + "...");
        }
        return ValidationResult.pass();
    }

    /**
     * Validate that every DALYTRAN-CARD-NUM in dailytran has a matching
     * XREF-CARD-NUM in cardxref.
     */
    public ValidationResult checkDailyTranToCardXref(
            List<Map<String, Object>> dailyTranRecords,
            List<Map<String, Object>> xrefRecords) {
        Set<String> xrefCards = extractFieldSet(xrefRecords, "XREF-CARD-NUM");
        List<String> orphans = new ArrayList<>();

        for (Map<String, Object> tran : dailyTranRecords) {
            String cardNum = DataFileParser.fieldValue(tran, "DALYTRAN-CARD-NUM");
            if (!cardNum.isEmpty() && !xrefCards.contains(cardNum)) {
                orphans.add(cardNum);
            }
        }

        if (!orphans.isEmpty()) {
            Set<String> unique = new HashSet<>(orphans);
            return ValidationResult.fail(
                    "Daily transactions have %d records (%d unique cards) not found in CARDXREF: %s",
                    orphans.size(), unique.size(),
                    unique.size() <= 5 ? unique.toString() : new ArrayList<>(unique).subList(0, 5) + "...");
        }
        return ValidationResult.pass();
    }

    /**
     * Validate that every TRANCAT-ACCT-ID in tcatbal has a matching ACCT-ID in acctdata.
     */
    public ValidationResult checkTcatbalToAcctData(
            List<Map<String, Object>> tcatbalRecords,
            List<Map<String, Object>> acctRecords) {
        Set<String> acctIds = extractFieldSet(acctRecords, "ACCT-ID");
        List<String> orphans = new ArrayList<>();

        for (Map<String, Object> tcat : tcatbalRecords) {
            String acctId = DataFileParser.fieldValue(tcat, "TRANCAT-ACCT-ID");
            if (!acctId.isEmpty() && !acctIds.contains(acctId)) {
                orphans.add(acctId);
            }
        }

        if (!orphans.isEmpty()) {
            return ValidationResult.fail(
                    "TCATBAL has %d account IDs not found in ACCTDATA: %s",
                    orphans.size(), orphans.size() <= 5 ? orphans.toString() : orphans.subList(0, 5) + "...");
        }
        return ValidationResult.pass();
    }

    /**
     * Validate that every DIS-ACCT-GROUP-ID in discgrp has a matching ACCT-GROUP-ID in acctdata.
     */
    public ValidationResult checkDiscGrpToAcctData(
            List<Map<String, Object>> discGrpRecords,
            List<Map<String, Object>> acctRecords) {
        Set<String> groupIds = extractFieldSet(acctRecords, "ACCT-GROUP-ID");
        List<String> orphans = new ArrayList<>();

        for (Map<String, Object> disc : discGrpRecords) {
            String groupId = DataFileParser.fieldValue(disc, "DIS-ACCT-GROUP-ID");
            if (!groupId.isEmpty() && !groupIds.contains(groupId)) {
                orphans.add(groupId);
            }
        }

        if (!orphans.isEmpty()) {
            Set<String> unique = new HashSet<>(orphans);
            return ValidationResult.fail(
                    "DISCGRP has %d records (%d unique group IDs) not found in ACCTDATA.ACCT-GROUP-ID: %s",
                    orphans.size(), unique.size(),
                    unique.size() <= 5 ? unique.toString() : new ArrayList<>(unique).subList(0, 5) + "...");
        }
        return ValidationResult.pass();
    }

    /**
     * Validate record count: expected number of records matches actual.
     */
    public ValidationResult checkRecordCount(
            List<Map<String, Object>> records, int expectedCount, String fileName) {
        if (records.size() != expectedCount) {
            return ValidationResult.fail(
                    "%s record count mismatch: expected=%d, actual=%d",
                    fileName, expectedCount, records.size());
        }
        return ValidationResult.pass();
    }

    /**
     * Validate numeric field sum across all records.
     */
    public ValidationResult checkNumericFieldSum(
            List<Map<String, Object>> records, String fieldName,
            BigDecimal expectedSum, String description) {
        BigDecimal actualSum = BigDecimal.ZERO;
        for (Map<String, Object> record : records) {
            actualSum = actualSum.add(DataFileParser.numericFieldValue(record, fieldName));
        }
        if (actualSum.compareTo(expectedSum) != 0) {
            return ValidationResult.fail(
                    "%s: expected sum=%s, actual sum=%s",
                    description, expectedSum.toPlainString(), actualSum.toPlainString());
        }
        return ValidationResult.pass();
    }

    private Set<String> extractFieldSet(List<Map<String, Object>> records, String fieldName) {
        Set<String> values = new HashSet<>();
        for (Map<String, Object> record : records) {
            String val = DataFileParser.fieldValue(record, fieldName);
            if (!val.isEmpty()) {
                values.add(val);
            }
        }
        return values;
    }
}
