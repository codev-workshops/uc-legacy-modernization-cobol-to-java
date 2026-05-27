package com.carddemo.entity;

import java.math.BigDecimal;

/**
 * Migrated from CVTRA07Y.cpy — Report formatting structures.
 * Multiple 01-levels mapped as inner classes.
 */
public final class ReportLayoutRecord {

    private ReportLayoutRecord() {
    }

    public static class ReportNameHeader {
        private String reptShortName;       // PIC X(38)
        private String reptLongName;        // PIC X(41)
        private String reptDateHeader;      // PIC X(12)
        private String reptStartDate;       // PIC X(10)
        private String reptEndDate;         // PIC X(10)

        public ReportNameHeader() {
        }

        public String getReptShortName() {
            return reptShortName;
        }

        public void setReptShortName(String reptShortName) {
            this.reptShortName = reptShortName;
        }

        public String getReptLongName() {
            return reptLongName;
        }

        public void setReptLongName(String reptLongName) {
            this.reptLongName = reptLongName;
        }

        public String getReptDateHeader() {
            return reptDateHeader;
        }

        public void setReptDateHeader(String reptDateHeader) {
            this.reptDateHeader = reptDateHeader;
        }

        public String getReptStartDate() {
            return reptStartDate;
        }

        public void setReptStartDate(String reptStartDate) {
            this.reptStartDate = reptStartDate;
        }

        public String getReptEndDate() {
            return reptEndDate;
        }

        public void setReptEndDate(String reptEndDate) {
            this.reptEndDate = reptEndDate;
        }
    }

    public static class TransactionDetailReport {
        private String tranReportTransId;       // PIC X(16)
        private String tranReportAccountId;     // PIC X(11)
        private String tranReportTypeCd;        // PIC X(02)
        private String tranReportTypeDesc;      // PIC X(15)
        private int tranReportCatCd;            // PIC 9(04)
        private String tranReportCatDesc;       // PIC X(29)
        private String tranReportSource;        // PIC X(10)
        private BigDecimal tranReportAmt;       // PIC -ZZZ,ZZZ,ZZZ.ZZ (display-formatted BigDecimal)

        public TransactionDetailReport() {
        }

        public String getTranReportTransId() {
            return tranReportTransId;
        }

        public void setTranReportTransId(String tranReportTransId) {
            this.tranReportTransId = tranReportTransId;
        }

        public String getTranReportAccountId() {
            return tranReportAccountId;
        }

        public void setTranReportAccountId(String tranReportAccountId) {
            this.tranReportAccountId = tranReportAccountId;
        }

        public String getTranReportTypeCd() {
            return tranReportTypeCd;
        }

        public void setTranReportTypeCd(String tranReportTypeCd) {
            this.tranReportTypeCd = tranReportTypeCd;
        }

        public String getTranReportTypeDesc() {
            return tranReportTypeDesc;
        }

        public void setTranReportTypeDesc(String tranReportTypeDesc) {
            this.tranReportTypeDesc = tranReportTypeDesc;
        }

        public int getTranReportCatCd() {
            return tranReportCatCd;
        }

        public void setTranReportCatCd(int tranReportCatCd) {
            this.tranReportCatCd = tranReportCatCd;
        }

        public String getTranReportCatDesc() {
            return tranReportCatDesc;
        }

        public void setTranReportCatDesc(String tranReportCatDesc) {
            this.tranReportCatDesc = tranReportCatDesc;
        }

        public String getTranReportSource() {
            return tranReportSource;
        }

        public void setTranReportSource(String tranReportSource) {
            this.tranReportSource = tranReportSource;
        }

        public BigDecimal getTranReportAmt() {
            return tranReportAmt;
        }

        public void setTranReportAmt(BigDecimal tranReportAmt) {
            this.tranReportAmt = tranReportAmt;
        }
    }

    public record TransactionHeader1(
            String transIdHeader,       // PIC X(17) "Transaction ID"
            String accountIdHeader,     // PIC X(12) "Account ID"
            String tranTypeHeader,      // PIC X(19) "Transaction Type"
            String tranCategoryHeader,  // PIC X(35) "Tran Category"
            String tranSourceHeader,    // PIC X(14) "Tran Source"
            String amountHeader         // PIC X(16) "        Amount"
    ) {
    }

    public record TransactionHeader2(
            String separatorLine        // PIC X(133) VALUE ALL '-'
    ) {
    }

    public static class ReportPageTotals {
        private BigDecimal reptPageTotal;   // PIC +ZZZ,ZZZ,ZZZ.ZZ

        public ReportPageTotals() {
        }

        public BigDecimal getReptPageTotal() {
            return reptPageTotal;
        }

        public void setReptPageTotal(BigDecimal reptPageTotal) {
            this.reptPageTotal = reptPageTotal;
        }
    }

    public static class ReportAccountTotals {
        private BigDecimal reptAccountTotal; // PIC +ZZZ,ZZZ,ZZZ.ZZ

        public ReportAccountTotals() {
        }

        public BigDecimal getReptAccountTotal() {
            return reptAccountTotal;
        }

        public void setReptAccountTotal(BigDecimal reptAccountTotal) {
            this.reptAccountTotal = reptAccountTotal;
        }
    }

    public static class ReportGrandTotals {
        private BigDecimal reptGrandTotal;  // PIC +ZZZ,ZZZ,ZZZ.ZZ

        public ReportGrandTotals() {
        }

        public BigDecimal getReptGrandTotal() {
            return reptGrandTotal;
        }

        public void setReptGrandTotal(BigDecimal reptGrandTotal) {
            this.reptGrandTotal = reptGrandTotal;
        }
    }
}
