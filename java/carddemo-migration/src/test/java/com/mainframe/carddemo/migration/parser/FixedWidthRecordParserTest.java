package com.mainframe.carddemo.migration.parser;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FixedWidthRecordParserTest {

    @Test
    void shouldParseSimpleAlphanumericFields() {
        List<FixedWidthField> layout = List.of(
                new FixedWidthField("NAME", 0, 10, FixedWidthField.FieldType.ALPHANUMERIC),
                new FixedWidthField("CODE", 10, 5, FixedWidthField.FieldType.ALPHANUMERIC));

        FixedWidthRecordParser parser = new FixedWidthRecordParser(layout);
        Map<String, Object> result = parser.parse("John      XY123");

        assertEquals("John", result.get("NAME"));
        assertEquals("XY123", result.get("CODE"));
    }

    @Test
    void shouldParseNumericFields() {
        List<FixedWidthField> layout = List.of(
                new FixedWidthField("ID", 0, 11, FixedWidthField.FieldType.NUMERIC),
                new FixedWidthField("COUNT", 11, 4, FixedWidthField.FieldType.NUMERIC));

        FixedWidthRecordParser parser = new FixedWidthRecordParser(layout);
        Map<String, Object> result = parser.parse("000000000010042");

        assertEquals(1L, result.get("ID"));
        assertEquals(42L, result.get("COUNT"));
    }

    @Test
    void shouldParseSignedNumericWithOverpunch() {
        List<FixedWidthField> layout = List.of(
                new FixedWidthField("AMOUNT", 0, 12, FixedWidthField.FieldType.SIGNED_NUMERIC, 2));

        FixedWidthRecordParser parser = new FixedWidthRecordParser(layout);

        Map<String, Object> result = parser.parse("00000001940{");
        assertEquals(0, new BigDecimal("194.00").compareTo((BigDecimal) result.get("AMOUNT")));

        result = parser.parse("00000009190J");
        assertEquals(0, new BigDecimal("-919.01").compareTo((BigDecimal) result.get("AMOUNT")));
    }

    @Test
    void shouldSkipFillerFields() {
        List<FixedWidthField> layout = List.of(
                new FixedWidthField("NAME", 0, 5, FixedWidthField.FieldType.ALPHANUMERIC),
                new FixedWidthField("FILLER", 5, 10, FixedWidthField.FieldType.FILLER),
                new FixedWidthField("CODE", 15, 3, FixedWidthField.FieldType.ALPHANUMERIC));

        FixedWidthRecordParser parser = new FixedWidthRecordParser(layout);
        Map<String, Object> result = parser.parse("Hello          XYZ");

        assertEquals("Hello", result.get("NAME"));
        assertEquals("XYZ", result.get("CODE"));
        assertNull(result.get("FILLER"));
    }

    @Test
    void shouldParseDateFields() {
        List<FixedWidthField> layout = List.of(
                new FixedWidthField("DATE", 0, 10, FixedWidthField.FieldType.DATE));

        FixedWidthRecordParser parser = new FixedWidthRecordParser(layout);
        Map<String, Object> result = parser.parse("2024-01-15");

        assertEquals("2024-01-15", result.get("DATE"));
    }

    @Test
    void shouldHandleShortLine() {
        List<FixedWidthField> layout = List.of(
                new FixedWidthField("FIELD1", 0, 5, FixedWidthField.FieldType.ALPHANUMERIC),
                new FixedWidthField("FIELD2", 5, 5, FixedWidthField.FieldType.ALPHANUMERIC),
                new FixedWidthField("FIELD3", 10, 5, FixedWidthField.FieldType.ALPHANUMERIC));

        FixedWidthRecordParser parser = new FixedWidthRecordParser(layout);
        Map<String, Object> result = parser.parse("Hello");

        assertEquals("Hello", result.get("FIELD1"));
        assertEquals("", result.get("FIELD2"));
        assertEquals("", result.get("FIELD3"));
    }

    @Test
    void shouldParseAccountLayout() {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.accountLayout());
        StringBuilder line = new StringBuilder();
        line.append("00000000001");  // ACCT-ID (11)
        line.append("Y");            // STATUS (1)
        line.append("00000001940{"); // BAL (12, signed with overpunch)
        line.append("00000020200{"); // CREDIT LIMIT (12)
        line.append("00000010200{"); // CASH LIMIT (12)
        line.append("2014-11-20");   // OPEN DATE (10)
        line.append("2025-05-20");   // EXP DATE (10)
        line.append("2025-05-20");   // REISSUE DATE (10)
        line.append("00000000000{"); // CYC CREDIT (12)
        line.append("00000000000{"); // CYC DEBIT (12)
        line.append("A000000000");   // ZIP (10)
        line.append("A000000000");   // GROUP (10)
        while (line.length() < 300) line.append(' ');

        Map<String, Object> result = parser.parse(line.toString());

        assertEquals(1L, result.get("ACCT-ID"));
        assertEquals("Y", result.get("ACCT-ACTIVE-STATUS"));
        assertEquals(0, new BigDecimal("194.00").compareTo((BigDecimal) result.get("ACCT-CURR-BAL")));
        assertEquals(0, new BigDecimal("2020.00").compareTo((BigDecimal) result.get("ACCT-CREDIT-LIMIT")));
        assertEquals("2014-11-20", result.get("ACCT-OPEN-DATE"));
        assertEquals("A000000000", result.get("ACCT-GROUP-ID"));
    }

    @Test
    void shouldParseTranTypeLayout() {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.tranTypeLayout());
        StringBuilder line = new StringBuilder();
        line.append("01");
        line.append(String.format("%-50s", "Purchase"));
        line.append(String.format("%-8s", ""));

        Map<String, Object> result = parser.parse(line.toString());

        assertEquals("01", result.get("TRAN-TYPE"));
        assertEquals("Purchase", result.get("TRAN-TYPE-DESC"));
    }

    @Test
    void shouldParseTranCatBalanceLayout() {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.tranCatBalanceLayout());
        StringBuilder line = new StringBuilder();
        line.append("00000000001");  // ACCT-ID (11)
        line.append("01");           // TYPE-CD (2)
        line.append("0001");         // CAT-CD (4)
        line.append("0000000500{"); // BAL (11, signed) = 50.00
        while (line.length() < 50) line.append(' ');

        Map<String, Object> result = parser.parse(line.toString());

        assertEquals(1L, result.get("TRANCAT-ACCT-ID"));
        assertEquals("01", result.get("TRANCAT-TYPE-CD"));
        assertEquals(1L, result.get("TRANCAT-CD"));
        assertEquals(0, new BigDecimal("50.00").compareTo((BigDecimal) result.get("TRAN-CAT-BAL")));
    }
}
