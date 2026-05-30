package com.carddemo.batch.job;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Formats report lines matching the COBOL CBTRN03C report layout
 * defined in copybook CVTRA07Y. All lines are padded to 133 characters.
 */
public final class ReportFormatter {

    static final int LINE_WIDTH = 133;
    static final int PAGE_SIZE = 20;

    private ReportFormatter() {}

    /**
     * REPORT-NAME-HEADER: DALYREPT (38) + title (41) + Date Range label (12)
     *                     + start date (10) + " to " (4) + end date (10) = 115 chars
     */
    public static String formatNameHeader(String startDate, String endDate) {
        StringBuilder sb = new StringBuilder(LINE_WIDTH);
        sb.append(padRight("DALYREPT", 38));
        sb.append(padRight("Daily Transaction Report", 41));
        sb.append("Date Range: ");
        sb.append(padRight(startDate != null ? startDate : "", 10));
        sb.append(" to ");
        sb.append(padRight(endDate != null ? endDate : "", 10));
        return pad(sb.toString(), LINE_WIDTH);
    }

    /**
     * TRANSACTION-HEADER-1: column titles matching CVTRA07Y layout.
     */
    public static String formatColumnHeader1() {
        StringBuilder sb = new StringBuilder(LINE_WIDTH);
        sb.append(padRight("Transaction ID", 17));
        sb.append(padRight("Account ID", 12));
        sb.append(padRight("Transaction Type", 19));
        sb.append(padRight("Tran Category", 35));
        sb.append(padRight("Tran Source", 14));
        sb.append(" ");
        sb.append(padLeft("Amount", 16));
        return pad(sb.toString(), LINE_WIDTH);
    }

    /**
     * TRANSACTION-HEADER-2: 133 dashes.
     */
    public static String formatSeparator() {
        return "-".repeat(LINE_WIDTH);
    }

    /**
     * Blank line (133 spaces).
     */
    public static String blankLine() {
        return " ".repeat(LINE_WIDTH);
    }

    /**
     * TRANSACTION-DETAIL-REPORT matching CVTRA07Y layout:
     * TransID(16) SP AccountID(11) SP TypeCD(2) '-' TypeDesc(15) SP
     * CatCD(4) '-' CatDesc(29) SP Source(10) SP(4) Amount(15) SP(2)
     */
    public static String formatDetailLine(TransactionReportItem item) {
        StringBuilder sb = new StringBuilder(LINE_WIDTH);
        sb.append(padRight(safe(item.getTranId()), 16));
        sb.append(" ");
        sb.append(padRight(safe(item.getAccountId()), 11));
        sb.append(" ");
        sb.append(padRight(safe(item.getTypeCd()), 2));
        sb.append("-");
        sb.append(padRight(safe(item.getTypeDesc()), 15));
        sb.append(" ");
        sb.append(String.format("%04d", item.getCatCd()));
        sb.append("-");
        sb.append(padRight(safe(item.getCatDesc()), 29));
        sb.append(" ");
        sb.append(padRight(safe(item.getSource()), 10));
        sb.append("    ");
        sb.append(formatDetailAmount(item.getAmount()));
        sb.append("  ");
        return pad(sb.toString(), LINE_WIDTH);
    }

    /**
     * REPORT-PAGE-TOTALS: "Page Total" (11) + dots (86) + amount (15) = 112
     */
    public static String formatPageTotal(BigDecimal total) {
        StringBuilder sb = new StringBuilder(LINE_WIDTH);
        sb.append(padRight("Page Total", 11));
        sb.append(".".repeat(86));
        sb.append(formatTotalAmount(total));
        return pad(sb.toString(), LINE_WIDTH);
    }

    /**
     * REPORT-ACCOUNT-TOTALS: "Account Total" (13) + dots (84) + amount (15) = 112
     */
    public static String formatAccountTotal(BigDecimal total) {
        StringBuilder sb = new StringBuilder(LINE_WIDTH);
        sb.append(padRight("Account Total", 13));
        sb.append(".".repeat(84));
        sb.append(formatTotalAmount(total));
        return pad(sb.toString(), LINE_WIDTH);
    }

    /**
     * REPORT-GRAND-TOTALS: "Grand Total" (11) + dots (86) + amount (15) = 112
     */
    public static String formatGrandTotal(BigDecimal total) {
        StringBuilder sb = new StringBuilder(LINE_WIDTH);
        sb.append(padRight("Grand Total", 11));
        sb.append(".".repeat(86));
        sb.append(formatTotalAmount(total));
        return pad(sb.toString(), LINE_WIDTH);
    }

    /**
     * COBOL PIC -ZZZ,ZZZ,ZZZ.ZZ — floating negative sign, 15-char field.
     * Positive values have a leading space; negative have '-'.
     */
    static String formatDetailAmount(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        String sign = amount.signum() < 0 ? "-" : " ";
        DecimalFormat df = new DecimalFormat(",##0.00");
        String num = df.format(amount.abs());
        return String.format("%15s", sign + num);
    }

    /**
     * COBOL PIC +ZZZ,ZZZ,ZZZ.ZZ — floating sign, 15-char field.
     * Positive values show '+'; negative show '-'.
     */
    static String formatTotalAmount(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        String sign = amount.signum() < 0 ? "-" : "+";
        DecimalFormat df = new DecimalFormat(",##0.00");
        String num = df.format(amount.abs());
        return String.format("%15s", sign + num);
    }

    static String padRight(String s, int length) {
        if (s.length() > length) {
            return s.substring(0, length);
        }
        return String.format("%-" + length + "s", s);
    }

    private static String padLeft(String s, int length) {
        if (s.length() > length) {
            return s.substring(0, length);
        }
        return String.format("%" + length + "s", s);
    }

    private static String pad(String s, int length) {
        if (s.length() >= length) {
            return s.substring(0, length);
        }
        return s + " ".repeat(length - s.length());
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
