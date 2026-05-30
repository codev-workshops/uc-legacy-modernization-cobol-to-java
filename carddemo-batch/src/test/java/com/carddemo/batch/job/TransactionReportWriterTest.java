package com.carddemo.batch.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.test.MetaDataInstanceFactory;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionReportWriterTest {

    @TempDir
    Path tempDir;

    private TransactionReportWriter writer;
    private String outputPath;

    @BeforeEach
    void setUp() {
        outputPath = tempDir.resolve("test-report.txt").toString();
        writer = new TransactionReportWriter(outputPath, "2024-01-01", "2024-01-31");
        writer.beforeStep(MetaDataInstanceFactory.createStepExecution());
    }

    private void closeWriter() {
        writer.afterStep(MetaDataInstanceFactory.createStepExecution());
    }

    @Test
    void write_singleItem_producesHeadersAndDetail() throws Exception {
        Chunk<TransactionReportItem> chunk = new Chunk<>(List.of(buildItem(
                "TX001", "00000000001", "SA", "Sale", 5001, "Online Purchase",
                "ONLINE", new BigDecimal("100.00"), "1111111111111111")));

        writer.write(chunk);
        closeWriter();

        List<String> lines = Files.readAllLines(Path.of(outputPath));
        assertTrue(lines.size() >= 5, "Should have headers + detail + totals");
        assertTrue(lines.get(0).contains("DALYREPT"));
        assertTrue(lines.get(0).contains("2024-01-01"));
        assertTrue(lines.get(2).contains("Transaction ID"));
        assertTrue(lines.get(3).startsWith("-".repeat(10)));
        assertTrue(lines.get(4).startsWith("TX001"));
    }

    @Test
    void write_twoCards_producesAccountTotals() throws Exception {
        TransactionReportItem item1 = buildItem(
                "TX001", "00000000001", "SA", "Sale", 5001, "Online Purchase",
                "ONLINE", new BigDecimal("100.00"), "1111111111111111");
        TransactionReportItem item2 = buildItem(
                "TX002", "00000000002", "RT", "Return", 5002, "Merchandise Return",
                "POS", new BigDecimal("-50.00"), "2222222222222222");

        writer.write(new Chunk<>(List.of(item1, item2)));
        closeWriter();

        String content = Files.readString(Path.of(outputPath));
        assertTrue(content.contains("Account Total"));
        assertTrue(content.contains("Page Total"));
        assertTrue(content.contains("Grand Total"));
    }

    @Test
    void write_pageBreak_triggeredAt20Lines() throws Exception {
        Chunk<TransactionReportItem> chunk = new Chunk<>();
        for (int i = 0; i < 20; i++) {
            chunk.add(buildItem(
                    String.format("TX%03d", i), "00000000001", "SA", "Sale",
                    5001, "Online Purchase", "ONLINE",
                    new BigDecimal("10.00"), "1111111111111111"));
        }

        writer.write(chunk);
        closeWriter();

        String content = Files.readString(Path.of(outputPath));
        long pageHeaders = content.lines()
                .filter(l -> l.contains("DALYREPT"))
                .count();
        assertTrue(pageHeaders >= 2, "Should have at least 2 page headers (page break at 20 lines)");
    }

    @Test
    void write_emptyChunk_noOutput() throws Exception {
        writer.write(new Chunk<>());
        closeWriter();

        List<String> lines = Files.readAllLines(Path.of(outputPath));
        assertTrue(lines.isEmpty(), "No items means no output");
    }

    @Test
    void write_allLinesCorrectWidth() throws Exception {
        TransactionReportItem item = buildItem(
                "TX001", "00000000001", "SA", "Sale", 5001, "Online Purchase",
                "ONLINE", new BigDecimal("100.00"), "1111111111111111");

        writer.write(new Chunk<>(List.of(item)));
        closeWriter();

        List<String> lines = Files.readAllLines(Path.of(outputPath));
        for (String line : lines) {
            assertEquals(ReportFormatter.LINE_WIDTH, line.length(),
                    "Line width mismatch: '" + line.substring(0, Math.min(40, line.length())) + "...'");
        }
    }

    @Test
    void write_grandTotal_accumulatesCorrectly() throws Exception {
        TransactionReportItem item1 = buildItem(
                "TX001", "00000000001", "SA", "Sale", 5001, "Online Purchase",
                "ONLINE", new BigDecimal("100.50"), "1111111111111111");
        TransactionReportItem item2 = buildItem(
                "TX002", "00000000001", "SA", "Sale", 5001, "Online Purchase",
                "ONLINE", new BigDecimal("200.25"), "1111111111111111");

        writer.write(new Chunk<>(List.of(item1, item2)));
        closeWriter();

        String content = Files.readString(Path.of(outputPath));
        assertTrue(content.contains("+300.75"),
                "Grand total should be 300.75 but content: " + content);
    }

    @Test
    void write_multipleChunks_statePreserved() throws Exception {
        Chunk<TransactionReportItem> chunk1 = new Chunk<>(List.of(buildItem(
                "TX001", "00000000001", "SA", "Sale", 5001, "Online Purchase",
                "ONLINE", new BigDecimal("100.00"), "1111111111111111")));
        Chunk<TransactionReportItem> chunk2 = new Chunk<>(List.of(buildItem(
                "TX002", "00000000001", "SA", "Sale", 5001, "Online Purchase",
                "ONLINE", new BigDecimal("200.00"), "1111111111111111")));

        writer.write(chunk1);
        writer.write(chunk2);
        closeWriter();

        String content = Files.readString(Path.of(outputPath));
        assertTrue(content.contains("TX001"));
        assertTrue(content.contains("TX002"));
        assertTrue(content.contains("+300.00"));
    }

    private TransactionReportItem buildItem(String tranId, String accountId,
                                            String typeCd, String typeDesc,
                                            int catCd, String catDesc,
                                            String source, BigDecimal amount,
                                            String cardNum) {
        TransactionReportItem item = new TransactionReportItem();
        item.setTranId(tranId);
        item.setAccountId(accountId);
        item.setTypeCd(typeCd);
        item.setTypeDesc(typeDesc);
        item.setCatCd(catCd);
        item.setCatDesc(catDesc);
        item.setSource(source);
        item.setAmount(amount);
        item.setCardNum(cardNum);
        return item;
    }
}
