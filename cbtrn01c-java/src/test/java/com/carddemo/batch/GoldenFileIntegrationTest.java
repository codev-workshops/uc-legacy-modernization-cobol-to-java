package com.carddemo.batch;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.processor.TransactionValidationProcessor;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.XrefRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test using golden-file JSON data.
 * Generates fixed-width test files from embedded JSON resources,
 * runs the Spring Batch job, and verifies validation outcomes.
 */
@SpringBatchTest
@SpringBootTest
class GoldenFileIntegrationTest {

    @TempDir
    static Path tempDir;

    private static Path dalytranFile;
    private static Path xrefFile;
    private static Path acctFile;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @DynamicPropertySource
    static void configureFiles(DynamicPropertyRegistry registry) throws IOException {
        generateTestFiles();
        registry.add("app.files.dalytran", () -> dalytranFile.toString());
        registry.add("app.files.xreffile", () -> xrefFile.toString());
        registry.add("app.files.acctfile", () -> acctFile.toString());
    }

    static void generateTestFiles() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Generate dailytran.txt from JSON
        dalytranFile = tempDir.resolve("dailytran.txt");
        try (InputStream is = GoldenFileIntegrationTest.class.getResourceAsStream("/golden/dailytran.json")) {
            if (is != null) {
                JsonNode root = mapper.readTree(is);
                List<String> lines = new ArrayList<>();
                for (JsonNode record : root.get("records")) {
                    lines.add(buildDailyTranLine(record));
                }
                Files.write(dalytranFile, lines);
            }
        }

        // Generate cardxref.txt from JSON
        xrefFile = tempDir.resolve("cardxref.txt");
        try (InputStream is = GoldenFileIntegrationTest.class.getResourceAsStream("/golden/cardxref.json")) {
            if (is != null) {
                JsonNode root = mapper.readTree(is);
                List<String> lines = new ArrayList<>();
                for (JsonNode record : root.get("records")) {
                    lines.add(buildXrefLine(record));
                }
                Files.write(xrefFile, lines);
            }
        }

        // Generate acctdata.txt from JSON
        acctFile = tempDir.resolve("acctdata.txt");
        try (InputStream is = GoldenFileIntegrationTest.class.getResourceAsStream("/golden/acctdata.json")) {
            if (is != null) {
                JsonNode root = mapper.readTree(is);
                List<String> lines = new ArrayList<>();
                for (JsonNode record : root.get("records")) {
                    lines.add(buildAcctLine(record));
                }
                Files.write(acctFile, lines);
            }
        }
    }

    @Test
    void shouldRunJobToCompletion() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void shouldProcessAllTransactions() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        // Read count = total transactions read by reader
        long readCount = execution.getStepExecutions().iterator().next().getReadCount();
        assertEquals(300, readCount, "Should read all 300 daily transactions");
    }

    @Test
    void shouldValidateCorrectTransactionCounts() throws Exception {
        // Manually compute expected: for each transaction, check if cardNum is in XREF
        ObjectMapper mapper = new ObjectMapper();
        int expectedValidated = 0;
        int expectedSkipped = 0;

        List<String> xrefCards = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/golden/cardxref.json")) {
            if (is != null) {
                JsonNode root = mapper.readTree(is);
                for (JsonNode record : root.get("records")) {
                    xrefCards.add(record.get("XREF-CARD-NUM").asText());
                }
            }
        }

        List<Long> acctIds = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/golden/acctdata.json")) {
            if (is != null) {
                JsonNode root = mapper.readTree(is);
                for (JsonNode record : root.get("records")) {
                    acctIds.add(record.get("ACCT-ID").asLong());
                }
            }
        }

        // Build xref map: cardNum → acctId
        java.util.Map<String, Long> xrefMap = new java.util.HashMap<>();
        try (InputStream is = getClass().getResourceAsStream("/golden/cardxref.json")) {
            if (is != null) {
                JsonNode root = mapper.readTree(is);
                for (JsonNode record : root.get("records")) {
                    xrefMap.put(record.get("XREF-CARD-NUM").asText(), record.get("XREF-ACCT-ID").asLong());
                }
            }
        }

        try (InputStream is = getClass().getResourceAsStream("/golden/dailytran.json")) {
            if (is != null) {
                JsonNode root = mapper.readTree(is);
                for (JsonNode record : root.get("records")) {
                    String cardNum = record.get("DALYTRAN-CARD-NUM").asText();
                    if (xrefMap.containsKey(cardNum)) {
                        long acctId = xrefMap.get(cardNum);
                        if (acctIds.contains(acctId)) {
                            expectedValidated++;
                        } else {
                            expectedSkipped++;
                        }
                    } else {
                        expectedSkipped++;
                    }
                }
            }
        }

        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        long writeCount = execution.getStepExecutions().iterator().next().getWriteCount();
        long filterCount = execution.getStepExecutions().iterator().next().getFilterCount();

        assertEquals(expectedValidated, writeCount, "Validated transaction count mismatch");
        assertEquals(expectedSkipped, filterCount, "Skipped transaction count mismatch");
        assertEquals(300, writeCount + filterCount, "Total should be 300 transactions");
    }

    // --- Helper methods to build fixed-width records from JSON ---

    private static String buildDailyTranLine(JsonNode record) {
        StringBuilder sb = new StringBuilder();
        sb.append(padRight(record.get("DALYTRAN-ID").asText(), 16));
        sb.append(padRight(record.get("DALYTRAN-TYPE-CD").asText(), 2));
        sb.append(padLeft(String.valueOf(record.get("DALYTRAN-CAT-CD").asInt()), 4, '0'));
        sb.append(padRight(record.get("DALYTRAN-SOURCE").asText(), 10));
        sb.append(padRight(record.get("DALYTRAN-DESC").asText(), 100));
        sb.append(encodeSignedDecimal(record.get("DALYTRAN-AMT").decimalValue(), 11));
        sb.append(padLeft(String.valueOf(record.get("DALYTRAN-MERCHANT-ID").asLong()), 9, '0'));
        sb.append(padRight(record.get("DALYTRAN-MERCHANT-NAME").asText(), 50));
        sb.append(padRight(record.get("DALYTRAN-MERCHANT-CITY").asText(), 50));
        sb.append(padRight(record.get("DALYTRAN-MERCHANT-ZIP").asText(), 10));
        sb.append(padRight(record.get("DALYTRAN-CARD-NUM").asText(), 16));
        sb.append(padRight(record.get("DALYTRAN-ORIG-TS").asText(), 26));
        sb.append(padRight(record.has("DALYTRAN-PROC-TS") ? record.get("DALYTRAN-PROC-TS").asText() : "", 26));
        sb.append(padRight(record.has("FILLER") ? record.get("FILLER").asText() : "", 20));
        return sb.toString();
    }

    private static String buildXrefLine(JsonNode record) {
        StringBuilder sb = new StringBuilder();
        sb.append(padRight(record.get("XREF-CARD-NUM").asText(), 16));
        sb.append(padLeft(String.valueOf(record.get("XREF-CUST-ID").asLong()), 9, '0'));
        sb.append(padLeft(String.valueOf(record.get("XREF-ACCT-ID").asLong()), 11, '0'));
        sb.append(padRight(record.has("FILLER") ? record.get("FILLER").asText() : "", 14));
        return sb.toString();
    }

    private static String buildAcctLine(JsonNode record) {
        StringBuilder sb = new StringBuilder();
        sb.append(padLeft(String.valueOf(record.get("ACCT-ID").asLong()), 11, '0'));
        sb.append(padRight(record.get("ACCT-ACTIVE-STATUS").asText(), 1));
        sb.append(encodeSignedDecimal(record.get("ACCT-CURR-BAL").decimalValue(), 12));
        sb.append(encodeSignedDecimal(record.get("ACCT-CREDIT-LIMIT").decimalValue(), 12));
        sb.append(encodeSignedDecimal(record.get("ACCT-CASH-CREDIT-LIMIT").decimalValue(), 12));
        sb.append(padRight(record.get("ACCT-OPEN-DATE").asText(), 10));
        sb.append(padRight(record.get("ACCT-EXPIRAION-DATE").asText(), 10));
        sb.append(padRight(record.get("ACCT-REISSUE-DATE").asText(), 10));
        sb.append(encodeSignedDecimal(record.get("ACCT-CURR-CYC-CREDIT").decimalValue(), 12));
        sb.append(encodeSignedDecimal(record.get("ACCT-CURR-CYC-DEBIT").decimalValue(), 12));
        sb.append(padRight(record.get("ACCT-ADDR-ZIP").asText(), 10));
        sb.append(padRight(record.has("ACCT-GROUP-ID") ? record.get("ACCT-GROUP-ID").asText() : "", 10));
        sb.append(" ".repeat(178)); // FILLER
        return sb.toString();
    }

    /**
     * Encodes a BigDecimal to COBOL zoned-decimal format with trailing sign overpunch.
     */
    private static String encodeSignedDecimal(BigDecimal value, int totalLength) {
        boolean negative = value.signum() < 0;
        BigDecimal absVal = value.abs();
        // Move decimal point right by 2 to get integer representation
        long intVal = absVal.movePointRight(2).longValue();
        String digits = String.valueOf(intVal);
        // Pad to totalLength - we'll replace last char with sign
        digits = padLeft(digits, totalLength, '0');

        char lastDigitChar = digits.charAt(digits.length() - 1);
        int lastDigit = lastDigitChar - '0';

        char signChar;
        if (negative) {
            signChar = lastDigit == 0 ? '}' : (char) ('J' + lastDigit - 1);
        } else {
            signChar = lastDigit == 0 ? '{' : (char) ('A' + lastDigit - 1);
        }

        return digits.substring(0, digits.length() - 1) + signChar;
    }

    private static String padRight(String s, int len) {
        if (s == null) s = "";
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }

    private static String padLeft(String s, int len, char padChar) {
        if (s.length() >= len) return s.substring(0, len);
        return String.valueOf(padChar).repeat(len - s.length()) + s;
    }
}
