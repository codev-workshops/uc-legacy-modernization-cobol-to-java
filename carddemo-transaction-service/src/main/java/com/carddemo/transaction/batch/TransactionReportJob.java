package com.carddemo.transaction.batch;

import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.entity.TransactionCategory;
import com.carddemo.transaction.entity.TransactionType;
import com.carddemo.transaction.repository.TransactionCategoryRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import jakarta.persistence.EntityManagerFactory;
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
public class TransactionReportJob {

    private final TransactionRepository transactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;

    @Value("${carddemo.batch.output-dir:output}")
    private String outputDir;

    @Bean
    public Job reportJob(JobRepository jobRepository, Step reportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(reportStep)
                .build();
    }

    @Bean
    public Step reportStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager) {
        return new StepBuilder("reportStep", jobRepository)
                .tasklet(reportTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet reportTasklet() {
        return new ReportTasklet();
    }

    public class ReportTasklet implements Tasklet {

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            List<Transaction> transactions = transactionRepository.findAll();
            log.info("Generating report for {} transactions", transactions.size());

            Map<String, String> typeDescs = transactionTypeRepository.findAll().stream()
                    .collect(Collectors.toMap(TransactionType::getTranType,
                            t -> t.getTranTypeDesc() != null ? t.getTranTypeDesc() : ""));

            Map<String, String> catDescs = transactionCategoryRepository.findAll().stream()
                    .collect(Collectors.toMap(
                            c -> c.getTranTypeCd() + "-" + c.getTranCatCd(),
                            c -> c.getTranCatTypeDesc() != null ? c.getTranCatTypeDesc() : ""));

            Path outPath = Paths.get(outputDir);
            Files.createDirectories(outPath);
            Path reportFile = outPath.resolve("daily_report.txt");

            try (BufferedWriter writer = Files.newBufferedWriter(reportFile)) {
                writeReportHeader(writer);
                writeColumnHeaders(writer);

                BigDecimal grandTotal = BigDecimal.ZERO;
                BigDecimal pageTotal = BigDecimal.ZERO;
                int lineCount = 0;

                for (Transaction t : transactions) {
                    String typeDesc = typeDescs.getOrDefault(
                            t.getTranTypeCd() != null ? t.getTranTypeCd().trim() : "", "");
                    String catKey = (t.getTranTypeCd() != null ? t.getTranTypeCd().trim() : "")
                            + "-" + t.getTranCatCd();
                    String catDesc = catDescs.getOrDefault(catKey, "");

                    writeDetailLine(writer, t, typeDesc, catDesc);

                    BigDecimal amt = t.getTranAmt() != null ? t.getTranAmt() : BigDecimal.ZERO;
                    pageTotal = pageTotal.add(amt);
                    grandTotal = grandTotal.add(amt);
                    lineCount++;

                    if (lineCount % 50 == 0) {
                        writePageTotal(writer, pageTotal);
                        pageTotal = BigDecimal.ZERO;
                        writer.newLine();
                        writeColumnHeaders(writer);
                    }
                }

                if (lineCount % 50 != 0) {
                    writePageTotal(writer, pageTotal);
                }

                writeGrandTotal(writer, grandTotal);
            }

            log.info("Report written to {}", reportFile.toAbsolutePath());
            return RepeatStatus.FINISHED;
        }
    }

    static void writeReportHeader(BufferedWriter writer) throws IOException {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        writer.write(String.format("%-38s%-41s%-12s%-10s to %-10s",
                "DALYREPT", "Daily Transaction Report", "Date Range: ", today, today));
        writer.newLine();
        writer.newLine();
    }

    static void writeColumnHeaders(BufferedWriter writer) throws IOException {
        writer.write(String.format("%-17s%-12s%-19s%-35s%-14s %16s",
                "Transaction ID", "Account ID", "Transaction Type",
                "Tran Category", "Tran Source", "Amount"));
        writer.newLine();
        writer.write("-".repeat(133));
        writer.newLine();
    }

    static void writeDetailLine(BufferedWriter writer, Transaction t,
                                String typeDesc, String catDesc) throws IOException {
        String tranId = padRight(t.getTranId() != null ? t.getTranId() : "", 16);
        String acctId = padRight(t.getTranCardNum() != null ? t.getTranCardNum() : "", 11);
        String typeCd = t.getTranTypeCd() != null ? t.getTranTypeCd().trim() : "";
        String typDescFmt = padRight(typeDesc, 15);
        String catCd = String.format("%04d", t.getTranCatCd() != null ? t.getTranCatCd() : 0);
        String catDescFmt = padRight(catDesc, 29);
        String source = padRight(t.getTranSource() != null ? t.getTranSource() : "", 10);
        BigDecimal amt = t.getTranAmt() != null ? t.getTranAmt() : BigDecimal.ZERO;
        String amtStr = formatAmount(amt);

        writer.write(String.format("%s %s %s-%s %s-%s %s    %s",
                tranId, acctId, typeCd, typDescFmt, catCd, catDescFmt, source, amtStr));
        writer.newLine();
    }

    static void writePageTotal(BufferedWriter writer, BigDecimal total) throws IOException {
        String label = "Page Total";
        String dots = ".".repeat(86);
        writer.write(String.format("%-11s%s%16s", label, dots, formatSignedAmount(total)));
        writer.newLine();
    }

    static void writeAccountTotal(BufferedWriter writer, BigDecimal total) throws IOException {
        String label = "Account Total";
        String dots = ".".repeat(84);
        writer.write(String.format("%-13s%s%16s", label, dots, formatSignedAmount(total)));
        writer.newLine();
    }

    static void writeGrandTotal(BufferedWriter writer, BigDecimal total) throws IOException {
        String label = "Grand Total";
        String dots = ".".repeat(86);
        writer.write(String.format("%-11s%s%16s", label, dots, formatSignedAmount(total)));
        writer.newLine();
    }

    static String formatAmount(BigDecimal amount) {
        if (amount.signum() < 0) {
            return String.format("-%,12.2f", amount.abs());
        }
        return String.format(" %,12.2f", amount);
    }

    static String formatSignedAmount(BigDecimal amount) {
        if (amount.signum() < 0) {
            return String.format("-%,12.2f", amount.abs());
        }
        return String.format("+%,12.2f", amount);
    }

    static String padRight(String s, int length) {
        if (s.length() >= length) return s.substring(0, length);
        return String.format("%-" + length + "s", s);
    }
}
