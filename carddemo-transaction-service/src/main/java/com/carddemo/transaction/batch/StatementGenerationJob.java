package com.carddemo.transaction.batch;

import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatementGenerationJob {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    @Value("${carddemo.batch.output-dir:output}")
    private String outputDir;

    @Bean
    public Job statementJob(JobRepository jobRepository, Step statementStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .start(statementStep)
                .build();
    }

    @Bean
    public Step statementStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
        return new StepBuilder("statementStep", jobRepository)
                .tasklet(statementTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet statementTasklet() {
        return new StatementTasklet();
    }

    public class StatementTasklet implements Tasklet {

        @Override
        @CircuitBreaker(name = "accountService")
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            List<Transaction> allTransactions = transactionRepository.findAll();

            Map<String, List<Transaction>> byCard = allTransactions.stream()
                    .filter(t -> t.getTranCardNum() != null)
                    .collect(Collectors.groupingBy(Transaction::getTranCardNum));

            Path statementsDir = Paths.get(outputDir, "statements");
            Files.createDirectories(statementsDir);

            for (Map.Entry<String, List<Transaction>> entry : byCard.entrySet()) {
                String cardNum = entry.getKey();
                List<Transaction> transactions = entry.getValue();

                AccountDto account = fetchAccount(cardNum);

                generateTextStatement(statementsDir, cardNum, account, transactions);
                generateHtmlStatement(statementsDir, cardNum, account, transactions);
            }

            log.info("Generated statements for {} accounts", byCard.size());
            return RepeatStatus.FINISHED;
        }

        private AccountDto fetchAccount(String cardNum) {
            try {
                var xref = accountServiceClient.getCardXref(cardNum);
                if (xref != null) {
                    return accountServiceClient.getAccount(xref.getXrefAcctId());
                }
            } catch (Exception e) {
                log.warn("Could not fetch account for card {}: {}", cardNum, e.getMessage());
            }
            return null;
        }
    }

    void generateTextStatement(Path dir, String cardNum,
                               AccountDto account, List<Transaction> transactions) throws IOException {
        Path file = dir.resolve("statement_" + cardNum + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writeTextHeader(writer, cardNum, account);
            writer.newLine();

            writer.write(String.format("%-16s %-10s %-30s %15s%n",
                    "Transaction ID", "Date", "Description", "Amount"));
            writer.write("-".repeat(75));
            writer.newLine();

            BigDecimal total = BigDecimal.ZERO;
            for (Transaction t : transactions) {
                BigDecimal amt = t.getTranAmt() != null ? t.getTranAmt() : BigDecimal.ZERO;
                writer.write(String.format("%-16s %-10s %-30s %,15.2f%n",
                        t.getTranId() != null ? t.getTranId() : "",
                        t.getTranOrigTs() != null ? t.getTranOrigTs().substring(0, Math.min(10, t.getTranOrigTs().length())) : "",
                        t.getTranDesc() != null ? truncate(t.getTranDesc(), 30) : "",
                        amt));
                total = total.add(amt);
            }

            writer.newLine();
            writer.write("-".repeat(75));
            writer.newLine();
            writeBalanceSummary(writer, account, total);
        }
        log.debug("Text statement generated: {}", file);
    }

    void generateHtmlStatement(Path dir, String cardNum,
                               AccountDto account, List<Transaction> transactions) throws IOException {
        Path file = dir.resolve("statement_" + cardNum + ".html");
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("<!DOCTYPE html>\n<html>\n<head>\n");
            writer.write("<title>Account Statement - " + cardNum + "</title>\n");
            writer.write("<style>body{font-family:monospace;margin:20px}table{border-collapse:collapse;width:100%}");
            writer.write("th,td{border:1px solid #ddd;padding:8px;text-align:left}");
            writer.write("th{background-color:#4472C4;color:white}.total{font-weight:bold}</style>\n");
            writer.write("</head>\n<body>\n");

            writer.write("<h1>Account Statement</h1>\n");
            writer.write("<p><strong>Card Number:</strong> " + cardNum + "</p>\n");
            if (account != null) {
                writer.write("<p><strong>Account ID:</strong> " + account.getAcctId() + "</p>\n");
                writer.write("<p><strong>Statement Date:</strong> "
                        + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "</p>\n");
            }

            writer.write("<table>\n<thead><tr>");
            writer.write("<th>Transaction ID</th><th>Date</th><th>Description</th><th>Amount</th>");
            writer.write("</tr></thead>\n<tbody>\n");

            BigDecimal total = BigDecimal.ZERO;
            for (Transaction t : transactions) {
                BigDecimal amt = t.getTranAmt() != null ? t.getTranAmt() : BigDecimal.ZERO;
                writer.write("<tr>");
                writer.write("<td>" + escapeHtml(t.getTranId()) + "</td>");
                writer.write("<td>" + escapeHtml(t.getTranOrigTs() != null
                        ? t.getTranOrigTs().substring(0, Math.min(10, t.getTranOrigTs().length())) : "") + "</td>");
                writer.write("<td>" + escapeHtml(t.getTranDesc()) + "</td>");
                writer.write(String.format("<td>%,.2f</td>", amt));
                writer.write("</tr>\n");
                total = total.add(amt);
            }

            writer.write("</tbody>\n</table>\n");

            writer.write("<div class=\"total\">\n");
            writer.write("<p>Total Transactions: " + transactions.size() + "</p>\n");
            writer.write(String.format("<p>Total Amount: %,.2f</p>%n", total));
            if (account != null) {
                if (account.getAcctCurrBal() != null) {
                    writer.write(String.format("<p>Current Balance: %,.2f</p>%n", account.getAcctCurrBal()));
                }
                if (account.getAcctCreditLimit() != null) {
                    writer.write(String.format("<p>Credit Limit: %,.2f</p>%n", account.getAcctCreditLimit()));
                }
            }
            writer.write("</div>\n");
            writer.write("</body>\n</html>");
        }
        log.debug("HTML statement generated: {}", file);
    }

    private static void writeTextHeader(BufferedWriter writer, String cardNum,
                                        AccountDto account) throws IOException {
        writer.write("=".repeat(75));
        writer.newLine();
        writer.write("                    ACCOUNT STATEMENT");
        writer.newLine();
        writer.write("=".repeat(75));
        writer.newLine();
        writer.write("Card Number: " + cardNum);
        writer.newLine();
        if (account != null) {
            writer.write("Account ID:  " + account.getAcctId());
            writer.newLine();
        }
        writer.write("Statement Date: " + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        writer.newLine();
        writer.write("=".repeat(75));
        writer.newLine();
    }

    private static void writeBalanceSummary(BufferedWriter writer, AccountDto account,
                                            BigDecimal transactionTotal) throws IOException {
        writer.write(String.format("%-50s %,15.2f%n", "Transaction Total:", transactionTotal));
        if (account != null) {
            if (account.getAcctCurrBal() != null) {
                writer.write(String.format("%-50s %,15.2f%n", "Current Balance:", account.getAcctCurrBal()));
            }
            if (account.getAcctCreditLimit() != null) {
                writer.write(String.format("%-50s %,15.2f%n", "Credit Limit:", account.getAcctCreditLimit()));
            }
            if (account.getAcctCurrCycCredit() != null) {
                writer.write(String.format("%-50s %,15.2f%n", "Current Cycle Credits:", account.getAcctCurrCycCredit()));
            }
            if (account.getAcctCurrCycDebit() != null) {
                writer.write(String.format("%-50s %,15.2f%n", "Current Cycle Debits:", account.getAcctCurrCycDebit()));
            }
        }
    }

    static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
