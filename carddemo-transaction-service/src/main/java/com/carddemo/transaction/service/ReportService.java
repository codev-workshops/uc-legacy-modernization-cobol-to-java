package com.carddemo.transaction.service;

import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.entity.TransactionCategory;
import com.carddemo.transaction.entity.TransactionType;
import com.carddemo.transaction.repository.TransactionCategoryRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;

    public byte[] generateTransactionReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating transaction report from {} to {}", startDate, endDate);

        List<Transaction> transactions = transactionRepository.findAll();

        Map<String, String> typeDescs = transactionTypeRepository.findAll().stream()
                .collect(Collectors.toMap(TransactionType::getTranType,
                        t -> t.getTranTypeDesc() != null ? t.getTranTypeDesc() : ""));

        Map<String, String> catDescs = transactionCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(
                        c -> c.getTranTypeCd() + "-" + c.getTranCatCd(),
                        c -> c.getTranCatTypeDesc() != null ? c.getTranCatTypeDesc() : ""));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            writeReportHeader(writer, startDate, endDate);
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

            writer.flush();
            log.info("Report generated with {} transactions", lineCount);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    static void writeReportHeader(BufferedWriter writer, LocalDate startDate,
                                  LocalDate endDate) throws IOException {
        String start = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String end = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        writer.write(String.format("%-38s%-41s%-12s%-10s to %-10s",
                "DALYREPT", "Daily Transaction Report", "Date Range: ", start, end));
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
