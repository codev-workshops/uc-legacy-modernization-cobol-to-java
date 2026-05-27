package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ReportLayoutRecordTest {

    @Test
    void reportNameHeaderFieldCount() {
        assertEquals(5, ReportLayoutRecord.ReportNameHeader.class.getDeclaredFields().length);
    }

    @Test
    void transactionDetailReportFieldCount() {
        assertEquals(8, ReportLayoutRecord.TransactionDetailReport.class.getDeclaredFields().length);
    }

    @Test
    void totalFieldsAreBigDecimal() throws NoSuchFieldException {
        assertEquals(BigDecimal.class,
                ReportLayoutRecord.TransactionDetailReport.class.getDeclaredField("tranReportAmt").getType());
        assertEquals(BigDecimal.class,
                ReportLayoutRecord.ReportPageTotals.class.getDeclaredField("reptPageTotal").getType());
        assertEquals(BigDecimal.class,
                ReportLayoutRecord.ReportAccountTotals.class.getDeclaredField("reptAccountTotal").getType());
        assertEquals(BigDecimal.class,
                ReportLayoutRecord.ReportGrandTotals.class.getDeclaredField("reptGrandTotal").getType());
    }

    @Test
    void canInstantiateInnerClasses() {
        var header = new ReportLayoutRecord.ReportNameHeader();
        header.setReptShortName("DALYREPT");
        header.setReptLongName("Daily Transaction Report");
        header.setReptDateHeader("Date Range: ");
        header.setReptStartDate("2024-01-01");
        header.setReptEndDate("2024-01-31");

        assertEquals("DALYREPT", header.getReptShortName());

        var detail = new ReportLayoutRecord.TransactionDetailReport();
        detail.setTranReportAmt(new BigDecimal("999.99"));
        assertEquals(new BigDecimal("999.99"), detail.getTranReportAmt());

        var pageTotals = new ReportLayoutRecord.ReportPageTotals();
        pageTotals.setReptPageTotal(new BigDecimal("5000.00"));
        assertEquals(new BigDecimal("5000.00"), pageTotals.getReptPageTotal());

        var acctTotals = new ReportLayoutRecord.ReportAccountTotals();
        acctTotals.setReptAccountTotal(new BigDecimal("25000.00"));
        assertEquals(new BigDecimal("25000.00"), acctTotals.getReptAccountTotal());

        var grandTotals = new ReportLayoutRecord.ReportGrandTotals();
        grandTotals.setReptGrandTotal(new BigDecimal("100000.00"));
        assertEquals(new BigDecimal("100000.00"), grandTotals.getReptGrandTotal());
    }
}
