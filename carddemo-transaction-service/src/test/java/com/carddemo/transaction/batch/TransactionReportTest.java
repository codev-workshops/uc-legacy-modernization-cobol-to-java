package com.carddemo.transaction.batch;

import com.carddemo.transaction.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionReportTest {

    @Test
    void testWriteReportHeader() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        TransactionReportJob.writeReportHeader(bw);
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("DALYREPT"));
        assertTrue(output.contains("Daily Transaction Report"));
        assertTrue(output.contains("Date Range:"));
    }

    @Test
    void testWriteColumnHeaders() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        TransactionReportJob.writeColumnHeaders(bw);
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("Transaction ID"));
        assertTrue(output.contains("Account ID"));
        assertTrue(output.contains("Transaction Type"));
        assertTrue(output.contains("Tran Category"));
        assertTrue(output.contains("Tran Source"));
        assertTrue(output.contains("Amount"));
        assertTrue(output.contains("-".repeat(133)));
    }

    @Test
    void testWriteDetailLine() throws Exception {
        Transaction t = Transaction.builder()
                .tranId("TRN0001")
                .tranCardNum("41111111111")
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranSource("ONLINE")
                .tranAmt(new BigDecimal("250.75"))
                .build();

        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        TransactionReportJob.writeDetailLine(bw, t, "Purchase", "Regular Sales Draft");
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("TRN0001"));
        assertTrue(output.contains("01"));
        assertTrue(output.contains("Purchase"));
        assertTrue(output.contains("0001"));
        assertTrue(output.contains("Regular Sales Draft"));
        assertTrue(output.contains("ONLINE"));
        assertTrue(output.contains("250.75"));
    }

    @Test
    void testWritePageTotal() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        TransactionReportJob.writePageTotal(bw, new BigDecimal("1500.50"));
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("Page Total"));
        assertTrue(output.contains("1,500.50"));
    }

    @Test
    void testWriteAccountTotal() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        TransactionReportJob.writeAccountTotal(bw, new BigDecimal("5000.00"));
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("Account Total"));
        assertTrue(output.contains("5,000.00"));
    }

    @Test
    void testWriteGrandTotal() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        TransactionReportJob.writeGrandTotal(bw, new BigDecimal("25000.00"));
        bw.flush();

        String output = sw.toString();
        assertTrue(output.contains("Grand Total"));
        assertTrue(output.contains("25,000.00"));
    }

    @Test
    void testFormatAmount() {
        assertEquals(" ", TransactionReportJob.formatAmount(new BigDecimal("0.00")).substring(0, 1));
        assertTrue(TransactionReportJob.formatAmount(new BigDecimal("-100.00")).startsWith("-"));
        assertTrue(TransactionReportJob.formatAmount(new BigDecimal("1234.56")).contains("1,234.56"));
    }

    @Test
    void testFormatSignedAmount() {
        assertTrue(TransactionReportJob.formatSignedAmount(new BigDecimal("100.00")).startsWith("+"));
        assertTrue(TransactionReportJob.formatSignedAmount(new BigDecimal("-100.00")).startsWith("-"));
    }

    @Test
    void testPadRight() {
        assertEquals("abc  ", TransactionReportJob.padRight("abc", 5));
        assertEquals("abcde", TransactionReportJob.padRight("abcdefgh", 5));
    }

    @Test
    void testWriteDetailLineWithNulls() throws Exception {
        Transaction t = Transaction.builder()
                .tranId(null)
                .tranCardNum(null)
                .tranTypeCd(null)
                .tranCatCd(null)
                .tranSource(null)
                .tranAmt(null)
                .build();

        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        TransactionReportJob.writeDetailLine(bw, t, "", "");
        bw.flush();

        String output = sw.toString();
        assertNotNull(output);
        assertFalse(output.isEmpty());
    }
}
