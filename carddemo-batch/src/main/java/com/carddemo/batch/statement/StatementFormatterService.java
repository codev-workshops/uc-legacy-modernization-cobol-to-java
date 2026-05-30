package com.carddemo.batch.statement;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Formats account statement text, replicating the layout produced by the
 * COBOL subroutine CBSTM03B and the driver program CBSTM03A.
 *
 * <p>Statement layout (80-char lines):
 * <pre>
 * *******************************START OF STATEMENT*******************************
 * Customer Name
 * Address Line 1
 * Address Line 2
 * City State Country ZIP
 * --------------------------------------------------------------------------------
 *                                  Basic Details
 * --------------------------------------------------------------------------------
 * Account ID         :value
 * Current Balance    :value
 * Credit Limit       :value
 * FICO Score         :value
 * --------------------------------------------------------------------------------
 *                               TRANSACTION SUMMARY
 * --------------------------------------------------------------------------------
 * Tran ID         Tran Details                                          Tran Amount
 * (detail lines)
 * --------------------------------------------------------------------------------
 * Total EXP:                                                        $    total
 * ********************************END OF STATEMENT********************************
 * </pre>
 */
@Service
public class StatementFormatterService {

    static final int LINE_WIDTH = 80;
    private static final String SEPARATOR = "-".repeat(LINE_WIDTH);
    private static final String STAR_LINE_START;
    private static final String STAR_LINE_END;

    static {
        String label = "START OF STATEMENT";
        int pad = LINE_WIDTH - label.length();
        int left = pad / 2;
        int right = pad - left;
        STAR_LINE_START = "*".repeat(left) + label + "*".repeat(right);

        label = "END OF STATEMENT";
        pad = LINE_WIDTH - label.length();
        left = pad / 2;
        right = pad - left;
        STAR_LINE_END = "*".repeat(left) + label + "*".repeat(right);
    }

    public List<String> formatStatement(StatementData data) {
        List<String> lines = new ArrayList<>();
        Customer cust = data.getCustomer();
        Account acct = data.getAccount();
        List<Transaction> txns = data.getTransactions();

        lines.add(STAR_LINE_START);

        lines.add(pad(buildCustomerName(cust), LINE_WIDTH));
        lines.add(pad(safe(cust.getAddrLine1()), LINE_WIDTH));
        lines.add(pad(safe(cust.getAddrLine2()), LINE_WIDTH));
        lines.add(pad(buildAddressLine3(cust), LINE_WIDTH));

        lines.add(SEPARATOR);
        lines.add(center("Basic Details", LINE_WIDTH));
        lines.add(SEPARATOR);

        lines.add(pad("Account ID         :" + String.valueOf(acct.getAcctId()), LINE_WIDTH));
        lines.add(pad("Current Balance    :" + formatAmount(acct.getCurrBal()), LINE_WIDTH));
        lines.add(pad("Credit Limit       :" + formatAmount(acct.getCreditLimit()), LINE_WIDTH));
        lines.add(pad("FICO Score         :" + (cust.getFicoCreditScore() != null
                ? String.valueOf(cust.getFicoCreditScore()) : ""), LINE_WIDTH));

        lines.add(SEPARATOR);
        lines.add(center("TRANSACTION SUMMARY", LINE_WIDTH));
        lines.add(SEPARATOR);

        lines.add(pad("Tran ID         "
                + "Tran Details                                       "
                + "  Tran Amount", LINE_WIDTH));
        lines.add(SEPARATOR);

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (Transaction tx : txns) {
            String tranId = rpad(safe(tx.getTranId()), 16);
            String detail = rpad(safe(tx.getDesc()), 50);
            String amt = formatTransactionAmount(tx.getAmt());
            lines.add(pad(tranId + " " + detail + "$" + amt, LINE_WIDTH));

            if (tx.getAmt() != null) {
                if (tx.getAmt().signum() >= 0) {
                    totalDebits = totalDebits.add(tx.getAmt());
                } else {
                    totalCredits = totalCredits.add(tx.getAmt().abs());
                }
            }
        }

        lines.add(SEPARATOR);

        BigDecimal totalExp = BigDecimal.ZERO;
        for (Transaction tx : txns) {
            if (tx.getAmt() != null) {
                totalExp = totalExp.add(tx.getAmt());
            }
        }
        lines.add(formatTotalLine("Total Debits:", totalDebits));
        lines.add(formatTotalLine("Total Credits:", totalCredits));
        lines.add(formatTotalLine("New Balance:", totalExp));

        lines.add(STAR_LINE_END);

        return lines;
    }

    String buildCustomerName(Customer cust) {
        StringBuilder sb = new StringBuilder();
        appendTrimmed(sb, cust.getFirstName());
        appendTrimmed(sb, cust.getMiddleName());
        appendTrimmed(sb, cust.getLastName());
        return sb.toString().trim();
    }

    String buildAddressLine3(Customer cust) {
        StringBuilder sb = new StringBuilder();
        appendTrimmed(sb, cust.getAddrLine3());
        appendTrimmed(sb, cust.getStateCode());
        appendTrimmed(sb, cust.getCountryCode());
        appendTrimmed(sb, cust.getZip());
        return sb.toString().trim();
    }

    private void appendTrimmed(StringBuilder sb, String value) {
        if (value != null && !value.isBlank()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(value.trim());
        }
    }

    String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2).toPlainString();
    }

    String formatTransactionAmount(BigDecimal amount) {
        if (amount == null) {
            return String.format("%12s", "0.00");
        }
        String formatted = amount.setScale(2).toPlainString();
        return String.format("%12s", formatted);
    }

    String formatTotalLine(String label, BigDecimal amount) {
        String amtStr = "$" + formatTransactionAmount(amount);
        int gap = LINE_WIDTH - label.length() - amtStr.length();
        if (gap < 1) gap = 1;
        return pad(label + " ".repeat(gap) + amtStr, LINE_WIDTH);
    }

    static String pad(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }

    static String rpad(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }

    static String center(String text, int width) {
        int totalPad = width - text.length();
        if (totalPad <= 0) return text.substring(0, width);
        int left = totalPad / 2;
        int right = totalPad - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private static String safe(String s) {
        return s != null ? s.trim() : "";
    }
}
