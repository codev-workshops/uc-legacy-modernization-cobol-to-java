package com.mainframe.carddemo.report.job;

import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.CustomerDto;
import com.mainframe.carddemo.common.dto.TransactionDto;
import com.mainframe.carddemo.report.entity.GeneratedReport;
import com.mainframe.carddemo.report.repository.GeneratedReportRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatementGenerationWriter implements ItemWriter<StatementData> {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final GeneratedReportRepository reportRepository;

    public StatementGenerationWriter(GeneratedReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public void write(Chunk<? extends StatementData> chunk) {
        for (StatementData data : chunk) {
            String textContent = buildTextStatement(data);
            String htmlContent = buildHtmlStatement(data);

            GeneratedReport report = new GeneratedReport();
            report.setAccountId(data.getAccount().getAccountId());
            report.setCustomerName(formatCustomerName(data.getCustomer()));
            report.setReportType("STATEMENT");
            report.setTextContent(textContent);
            report.setHtmlContent(htmlContent);
            report.setGeneratedAt(LocalDateTime.now());

            reportRepository.save(report);
        }
    }

    private String formatCustomerName(CustomerDto customer) {
        if (customer == null) return "Unknown";
        StringBuilder sb = new StringBuilder();
        if (customer.getFirstName() != null) sb.append(customer.getFirstName());
        if (customer.getLastName() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(customer.getLastName());
        }
        return sb.length() > 0 ? sb.toString() : "Unknown";
    }

    private String buildTextStatement(StatementData data) {
        AccountDto acct = data.getAccount();
        CustomerDto cust = data.getCustomer();
        List<TransactionDto> txns = data.getTransactions();

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================\n");
        sb.append("                    ACCOUNT STATEMENT                      \n");
        sb.append("==========================================================\n\n");

        sb.append("Customer: ").append(formatCustomerName(cust)).append("\n");
        if (cust != null) {
            if (cust.getAddressLine1() != null) sb.append("Address:  ").append(cust.getAddressLine1()).append("\n");
            if (cust.getStateCode() != null) sb.append("State:    ").append(cust.getStateCode());
            if (cust.getZip() != null) sb.append("  ZIP: ").append(cust.getZip());
            sb.append("\n");
        }
        sb.append("\n");

        sb.append("Account #:    ").append(acct.getAccountId()).append("\n");
        sb.append("Card #:       ").append(data.getCardNum()).append("\n");
        sb.append("Balance:      $").append(fmt(acct.getCurrentBalance())).append("\n");
        sb.append("Credit Limit: $").append(fmt(acct.getCreditLimit())).append("\n");
        sb.append("Available:    $").append(fmt(available(acct))).append("\n\n");

        sb.append("----------------------------------------------------------\n");
        sb.append(String.format("%-20s %-30s %10s\n", "DATE", "DESCRIPTION", "AMOUNT"));
        sb.append("----------------------------------------------------------\n");

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (TransactionDto tx : txns) {
            String date = tx.getOrigTimestamp() != null ? tx.getOrigTimestamp().format(DATE_FMT) : "";
            String desc = tx.getDescription() != null ? tx.getDescription() : "";
            if (desc.length() > 30) desc = desc.substring(0, 30);
            BigDecimal amt = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;

            sb.append(String.format("%-20s %-30s %10s\n", date, desc, "$" + fmt(amt)));

            if (amt.compareTo(BigDecimal.ZERO) >= 0) {
                totalDebits = totalDebits.add(amt);
            } else {
                totalCredits = totalCredits.add(amt.abs());
            }
        }

        sb.append("----------------------------------------------------------\n");
        sb.append(String.format("Total Debits:  $%s\n", fmt(totalDebits)));
        sb.append(String.format("Total Credits: $%s\n", fmt(totalCredits)));
        sb.append(String.format("New Balance:   $%s\n", fmt(acct.getCurrentBalance())));
        sb.append("==========================================================\n");

        return sb.toString();
    }

    private String buildHtmlStatement(StatementData data) {
        AccountDto acct = data.getAccount();
        CustomerDto cust = data.getCustomer();
        List<TransactionDto> txns = data.getTransactions();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Account Statement</title>");
        sb.append("<style>body{font-family:monospace;margin:20px;}table{border-collapse:collapse;width:100%;}");
        sb.append("th,td{border:1px solid #ccc;padding:8px;text-align:left;}th{background:#f0f0f0;}</style></head><body>");

        sb.append("<h1>Account Statement</h1>");
        sb.append("<div class=\"header\">");
        sb.append("<p><strong>Customer:</strong> ").append(esc(formatCustomerName(cust))).append("</p>");
        if (cust != null && cust.getAddressLine1() != null) {
            sb.append("<p><strong>Address:</strong> ").append(esc(cust.getAddressLine1())).append("</p>");
        }
        sb.append("</div>");

        sb.append("<div class=\"summary\">");
        sb.append("<p><strong>Account #:</strong> ").append(acct.getAccountId()).append("</p>");
        sb.append("<p><strong>Card #:</strong> ").append(esc(data.getCardNum())).append("</p>");
        sb.append("<p><strong>Balance:</strong> $").append(fmt(acct.getCurrentBalance())).append("</p>");
        sb.append("<p><strong>Credit Limit:</strong> $").append(fmt(acct.getCreditLimit())).append("</p>");
        sb.append("<p><strong>Available Credit:</strong> $").append(fmt(available(acct))).append("</p>");
        sb.append("</div>");

        sb.append("<table><thead><tr><th>Date</th><th>Description</th><th>Amount</th></tr></thead><tbody>");

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (TransactionDto tx : txns) {
            String date = tx.getOrigTimestamp() != null ? tx.getOrigTimestamp().format(DATE_FMT) : "";
            String desc = tx.getDescription() != null ? tx.getDescription() : "";
            BigDecimal amt = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;

            sb.append("<tr><td>").append(esc(date)).append("</td>");
            sb.append("<td>").append(esc(desc)).append("</td>");
            sb.append("<td>$").append(fmt(amt)).append("</td></tr>");

            if (amt.compareTo(BigDecimal.ZERO) >= 0) {
                totalDebits = totalDebits.add(amt);
            } else {
                totalCredits = totalCredits.add(amt.abs());
            }
        }

        sb.append("</tbody></table>");
        sb.append("<div class=\"totals\">");
        sb.append("<p><strong>Total Debits:</strong> $").append(fmt(totalDebits)).append("</p>");
        sb.append("<p><strong>Total Credits:</strong> $").append(fmt(totalCredits)).append("</p>");
        sb.append("<p><strong>New Balance:</strong> $").append(fmt(acct.getCurrentBalance())).append("</p>");
        sb.append("</div>");
        sb.append("</body></html>");

        return sb.toString();
    }

    private String fmt(BigDecimal v) {
        return v != null ? v.setScale(2).toPlainString() : "0.00";
    }

    private BigDecimal available(AccountDto acct) {
        BigDecimal limit = acct.getCreditLimit() != null ? acct.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal bal = acct.getCurrentBalance() != null ? acct.getCurrentBalance() : BigDecimal.ZERO;
        return limit.subtract(bal);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
