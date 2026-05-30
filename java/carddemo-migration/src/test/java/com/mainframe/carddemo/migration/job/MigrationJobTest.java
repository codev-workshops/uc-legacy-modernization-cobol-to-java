package com.mainframe.carddemo.migration.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema-migration.sql"},
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MigrationJobTest {

    private static final AtomicLong RUN_ID = new AtomicLong(System.currentTimeMillis());

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("migrationJob")
    private Job migrationJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(migrationJob);
    }

    private JobParameters buildParams() {
        return new JobParametersBuilder()
                .addString("dataDir", tempDir.toString())
                .addLong("run.id", RUN_ID.incrementAndGet())
                .toJobParameters();
    }

    @Test
    void shouldMigrateAllDataFiles() throws Exception {
        createTestDataFiles();

        JobExecution execution = jobLauncherTestUtils.launchJob(buildParams());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        assertEquals(1, countRows("account"));
        assertEquals(1, countRows("customer"));
        assertEquals(1, countRows("card"));
        assertEquals(1, countRows("card_xref"));
        assertEquals(1, countRows("daily_transaction"));
        assertEquals(1, countRows("tran_type"));
        assertEquals(1, countRows("tran_category"));
        assertEquals(1, countRows("tran_cat_balance"));
        assertEquals(1, countRows("disclosure_group"));
    }

    @Test
    void shouldParseSignedOverpunchCorrectly() throws Exception {
        createTestDataFiles();

        JobExecution execution = jobLauncherTestUtils.launchJob(buildParams());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT acct_curr_bal FROM account WHERE acct_id = 1", BigDecimal.class);
        assertEquals(0, new BigDecimal("194.00").compareTo(balance));
    }

    @Test
    void shouldParseFixedWidthFields() throws Exception {
        createTestDataFiles();

        JobExecution execution = jobLauncherTestUtils.launchJob(buildParams());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        String status = jdbcTemplate.queryForObject(
                "SELECT acct_active_status FROM account WHERE acct_id = 1", String.class);
        assertEquals("Y", status);

        String typeDesc = jdbcTemplate.queryForObject(
                "SELECT tran_type_desc FROM tran_type WHERE tran_type = '01'", String.class);
        assertEquals("Purchase", typeDesc);
    }

    @Test
    void shouldValidateRecordCounts() throws Exception {
        createTestDataFiles();

        JobExecution execution = jobLauncherTestUtils.launchJob(buildParams());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        MigrationValidator validator = new MigrationValidator(jdbcTemplate);
        var results = validator.validate(tempDir.toString());

        assertTrue(results.get("account").passed());
        assertTrue(results.get("tran_type").passed());
        assertTrue(results.get("disclosure_group").passed());
    }

    private void createTestDataFiles() throws IOException {
        // Account: 300 bytes, CVACT01Y layout
        String acctRecord = pad("00000000001", 11) + "Y" +
                "00000001940{" + "00000020200{" + "00000010200{" +
                "2014-11-20" + "2025-05-20" + "2025-05-20" +
                "00000000000{" + "00000000000{" +
                pad("A000000000", 10) + pad("A000000000", 10) +
                pad("", 178);
        writeFile("acctdata.txt", acctRecord);

        // Customer: 500 bytes
        String custRecord = pad("000000001", 9) +
                pad("Alice", 25) + pad("M", 25) + pad("Johnson", 25) +
                pad("123 Main St", 50) + pad("Apt 1", 50) + pad("", 50) +
                pad("NY", 2) + pad("USA", 3) + pad("10001", 10) +
                pad("5551234567", 15) + pad("5559876543", 15) +
                pad("123456789", 9) + pad("DL12345", 20) +
                "1980-01-15" + pad("0000000001", 10) + "Y" + pad("750", 3) +
                pad("", 168);
        writeFile("custdata.txt", custRecord);

        // Card: 150 bytes
        String cardRecord = pad("4111111111111111", 16) + pad("00000000001", 11) + pad("123", 3) +
                pad("ALICE JOHNSON", 50) + "2025-12-31" + "Y" + pad("", 59);
        writeFile("carddata.txt", cardRecord);

        // Card xref: 36 bytes
        String xrefRecord = "411111111111111100000000100000000001";
        writeFile("cardxref.txt", xrefRecord);

        // Daily transaction: 350 bytes
        String tranRecord = pad("TX00000000000001", 16) + pad("01", 2) + pad("0001", 4) +
                pad("POS TERM", 10) + pad("Purchase at Store", 100) +
                "0000005000{" + pad("800000000", 9) + pad("Test Store", 50) +
                pad("New York", 50) + pad("10001", 10) + pad("4111111111111111", 16) +
                pad("2024-01-15 10:30:00.000000", 26) + pad("2024-01-15 10:30:01.000000", 26) +
                pad("", 20);
        writeFile("dailytran.txt", tranRecord);

        // Tran type: 60 bytes
        String typeRecord = pad("01", 2) + pad("Purchase", 50) + pad("", 8);
        writeFile("trantype.txt", typeRecord);

        // Tran category: 60 bytes
        String catRecord = pad("01", 2) + pad("0001", 4) + pad("Regular Sales Draft", 50) + pad("", 4);
        writeFile("trancatg.txt", catRecord);

        // Tran cat balance: 50 bytes (S9(09)V99 = 11 bytes for balance)
        // 50.00 → "0000000500{" (11 chars)
        String balRecord = pad("00000000001", 11) + pad("01", 2) + pad("0001", 4) +
                "0000000500{" + pad("", 22);
        writeFile("tcatbal.txt", balRecord);

        // Disclosure group: 50 bytes (S9(04)V99 = 6 bytes for rate)
        // 15.00 → "00150{" (6 chars)
        String discRecord = pad("A000000000", 10) + pad("01", 2) + pad("0001", 4) +
                "00150{" + pad("", 28);
        writeFile("discgrp.txt", discRecord);
    }

    private void writeFile(String name, String content) throws IOException {
        Files.writeString(tempDir.resolve(name), content + "\n");
    }

    private String pad(String value, int length) {
        if (value.length() >= length) return value.substring(0, length);
        return value + " ".repeat(length - value.length());
    }

    private int countRows(String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }
}
