package com.carddemo.batch.job;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportFormatterTest {

    @Test
    void formatNameHeader_containsExpectedParts() {
        String header = ReportFormatter.formatNameHeader("2024-01-01", "2024-01-31");
        assertEquals(ReportFormatter.LINE_WIDTH, header.length());
        assertTrue(header.startsWith("DALYREPT"));
        assertTrue(header.contains("Daily Transaction Report"));
        assertTrue(header.contains("Date Range: "));
        assertTrue(header.contains("2024-01-01"));
        assertTrue(header.contains(" to "));
        assertTrue(header.contains("2024-01-31"));
    }

    @Test
    void formatNameHeader_nullDates() {
        String header = ReportFormatter.formatNameHeader(null, null);
        assertEquals(ReportFormatter.LINE_WIDTH, header.length());
        assertTrue(header.contains("Date Range: "));
    }

    @Test
    void formatColumnHeader1_correctWidth() {
        String header = ReportFormatter.formatColumnHeader1();
        assertEquals(ReportFormatter.LINE_WIDTH, header.length());
        assertTrue(header.contains("Transaction ID"));
        assertTrue(header.contains("Account ID"));
        assertTrue(header.contains("Transaction Type"));
        assertTrue(header.contains("Tran Category"));
        assertTrue(header.contains("Tran Source"));
        assertTrue(header.contains("Amount"));
    }

    @Test
    void formatSeparator_allDashes() {
        String sep = ReportFormatter.formatSeparator();
        assertEquals(ReportFormatter.LINE_WIDTH, sep.length());
        assertEquals("-".repeat(ReportFormatter.LINE_WIDTH), sep);
    }

    @Test
    void blankLine_allSpaces() {
        String blank = ReportFormatter.blankLine();
        assertEquals(ReportFormatter.LINE_WIDTH, blank.length());
        assertTrue(blank.isBlank());
    }

    @Test
    void formatDetailLine_correctLayout() {
        TransactionReportItem item = new TransactionReportItem();
        item.setTranId("0000000000000001");
        item.setAccountId("00000000001");
        item.setTypeCd("SA");
        item.setTypeDesc("Sale");
        item.setCatCd(5001);
        item.setCatDesc("Online Purchase");
        item.setSource("ONLINE");
        item.setAmount(new BigDecimal("1234.56"));

        String line = ReportFormatter.formatDetailLine(item);
        assertEquals(ReportFormatter.LINE_WIDTH, line.length());
        assertTrue(line.startsWith("0000000000000001"));
        assertTrue(line.contains("00000000001"));
        assertTrue(line.contains("SA-Sale"));
        assertTrue(line.contains("5001-Online Purchase"));
        assertTrue(line.contains("ONLINE"));
        assertTrue(line.contains("1,234.56"));
    }

    @Test
    void formatDetailLine_nullFields() {
        TransactionReportItem item = new TransactionReportItem();
        item.setCatCd(0);
        item.setAmount(BigDecimal.ZERO);

        String line = ReportFormatter.formatDetailLine(item);
        assertEquals(ReportFormatter.LINE_WIDTH, line.length());
    }

    @Test
    void formatDetailAmount_positive() {
        String amt = ReportFormatter.formatDetailAmount(new BigDecimal("123.45"));
        assertEquals(15, amt.length());
        assertTrue(amt.contains("123.45"));
        assertEquals(' ', amt.charAt(amt.indexOf('1') - 1));
    }

    @Test
    void formatDetailAmount_negative() {
        String amt = ReportFormatter.formatDetailAmount(new BigDecimal("-12345.67"));
        assertEquals(15, amt.length());
        assertTrue(amt.contains("-12,345.67"));
    }

    @Test
    void formatDetailAmount_zero() {
        String amt = ReportFormatter.formatDetailAmount(BigDecimal.ZERO);
        assertEquals(15, amt.length());
        assertTrue(amt.contains("0.00"));
    }

    @Test
    void formatDetailAmount_null() {
        String amt = ReportFormatter.formatDetailAmount(null);
        assertEquals(15, amt.length());
        assertTrue(amt.contains("0.00"));
    }

    @Test
    void formatDetailAmount_large() {
        String amt = ReportFormatter.formatDetailAmount(new BigDecimal("999999999.99"));
        assertEquals(15, amt.length());
        assertTrue(amt.contains("999,999,999.99"));
    }

    @Test
    void formatTotalAmount_positive() {
        String amt = ReportFormatter.formatTotalAmount(new BigDecimal("300.50"));
        assertEquals(15, amt.length());
        assertTrue(amt.contains("+300.50"));
    }

    @Test
    void formatTotalAmount_negative() {
        String amt = ReportFormatter.formatTotalAmount(new BigDecimal("-50.00"));
        assertEquals(15, amt.length());
        assertTrue(amt.contains("-50.00"));
    }

    @Test
    void formatTotalAmount_zero() {
        String amt = ReportFormatter.formatTotalAmount(BigDecimal.ZERO);
        assertEquals(15, amt.length());
        assertTrue(amt.contains("+0.00"));
    }

    @Test
    void formatTotalAmount_null() {
        String amt = ReportFormatter.formatTotalAmount(null);
        assertEquals(15, amt.length());
        assertTrue(amt.contains("+0.00"));
    }

    @Test
    void formatPageTotal_layout() {
        String line = ReportFormatter.formatPageTotal(new BigDecimal("250.50"));
        assertEquals(ReportFormatter.LINE_WIDTH, line.length());
        assertTrue(line.startsWith("Page Total"));
        assertTrue(line.contains(".."));
        assertTrue(line.contains("+250.50"));
    }

    @Test
    void formatAccountTotal_layout() {
        String line = ReportFormatter.formatAccountTotal(new BigDecimal("300.50"));
        assertEquals(ReportFormatter.LINE_WIDTH, line.length());
        assertTrue(line.startsWith("Account Total"));
        assertTrue(line.contains(".."));
        assertTrue(line.contains("+300.50"));
    }

    @Test
    void formatGrandTotal_layout() {
        String line = ReportFormatter.formatGrandTotal(new BigDecimal("250.50"));
        assertEquals(ReportFormatter.LINE_WIDTH, line.length());
        assertTrue(line.startsWith("Grand Total"));
        assertTrue(line.contains(".."));
        assertTrue(line.contains("+250.50"));
    }

    @Test
    void padRight_truncatesLongString() {
        String result = ReportFormatter.padRight("Hello World", 5);
        assertEquals("Hello", result);
    }

    @Test
    void padRight_padsShortString() {
        String result = ReportFormatter.padRight("Hi", 5);
        assertEquals("Hi   ", result);
    }
}
