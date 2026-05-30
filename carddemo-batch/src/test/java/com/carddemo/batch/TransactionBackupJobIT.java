package com.carddemo.batch;

import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBatchTest
@SpringBootTest(classes = BatchApplication.class)
@ActiveProfiles("dev")
class TransactionBackupJobIT {

    private static Path tempDir;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("transactionBackupJob")
    private Job transactionBackupJob;

    @Autowired
    private TransactionRepository transactionRepository;

    @DynamicPropertySource
    static void configureOutputPath(DynamicPropertyRegistry registry) {
        try {
            tempDir = Files.createTempDirectory("backup-test");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        registry.add("batch.backup.output-path",
                () -> tempDir.resolve("test_backup.dat").toString());
    }

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(transactionBackupJob);
        transactionRepository.deleteAll();
    }

    @Test
    void jobCompletesSuccessfullyWithData() throws Exception {
        transactionRepository.saveAll(List.of(
                createTransaction("0000000000000001", "SA", 5000, "ONLINE",
                        "Coffee purchase", new BigDecimal("4.50"), 100000001L,
                        "Starbucks", "Seattle", "98101",
                        "4111111111111111", "2024-01-15 10:30:00.000000",
                        "2024-01-15 11:00:00.000000"),
                createTransaction("0000000000000002", "CR", 6000, "BATCH",
                        "Refund", new BigDecimal("25.00"), 100000002L,
                        "Amazon", "New York", "10001",
                        "5222222222222222", "2024-02-20 14:30:00.000000",
                        "2024-02-20 15:00:00.000000"),
                createTransaction("0000000000000003", "DB", 7000, "ATM",
                        "Cash withdrawal", new BigDecimal("200.00"), 100000003L,
                        "Chase ATM", "Chicago", "60601",
                        "3333444455556666", "2024-03-10 09:15:00.000000",
                        null)
        ));

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(ExitStatus.COMPLETED, execution.getExitStatus());

        Path backupFile = tempDir.resolve("test_backup.dat");
        assertTrue(Files.exists(backupFile), "Backup file should exist");

        List<String> lines = Files.readAllLines(backupFile);
        assertEquals(3, lines.size(), "Should have 3 transaction lines");

        assertTrue(lines.get(0).contains("0000000000000001"));
        assertTrue(lines.get(0).contains("4.50"));
        assertTrue(lines.get(0).contains("Starbucks"));
        assertTrue(lines.get(0).contains("20240115"));

        assertTrue(lines.get(1).contains("0000000000000002"));
        assertTrue(lines.get(1).contains("25.00"));

        assertTrue(lines.get(2).contains("0000000000000003"));
        assertTrue(lines.get(2).contains("200.00"));
    }

    @Test
    void jobCompletesWithEmptyTable() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        Path backupFile = tempDir.resolve("test_backup.dat");
        assertTrue(Files.exists(backupFile), "Backup file should exist even with no data");

        List<String> lines = Files.readAllLines(backupFile);
        assertEquals(0, lines.size(), "Should have zero lines for empty table");
    }

    @Test
    void stepExecutionMetrics() throws Exception {
        transactionRepository.saveAll(List.of(
                createTransaction("0000000000000010", "SA", 1000, "WEB",
                        "Online order", new BigDecimal("50.00"), 200000001L,
                        "eBay", "San Jose", "95110",
                        "6666777788889999", "2024-04-01 12:00:00.000000",
                        "2024-04-01 12:30:00.000000"),
                createTransaction("0000000000000011", "SA", 1001, "POS",
                        "In-store swipe", new BigDecimal("15.75"), 200000002L,
                        "Target", "Minneapolis", "55403",
                        "1111222233334444", "2024-04-02 16:45:00.000000",
                        "2024-04-02 17:00:00.000000")
        ));

        JobExecution execution = jobLauncherTestUtils.launchJob();

        StepExecution stepExecution = execution.getStepExecutions().iterator().next();
        assertEquals(2, stepExecution.getWriteCount(), "Should write 2 records");
        assertEquals(2, stepExecution.getReadCount(), "Should read 2 records");
    }

    private Transaction createTransaction(String tranId, String typeCd, int catCd,
                                           String source, String desc, BigDecimal amt,
                                           Long merchantId, String merchantName,
                                           String merchantCity, String merchantZip,
                                           String cardNum, String origTs, String procTs) {
        Transaction txn = new Transaction();
        txn.setTranId(tranId);
        txn.setTypeCd(typeCd);
        txn.setCatCd(catCd);
        txn.setSource(source);
        txn.setDesc(desc);
        txn.setAmt(amt);
        txn.setMerchantId(merchantId);
        txn.setMerchantName(merchantName);
        txn.setMerchantCity(merchantCity);
        txn.setMerchantZip(merchantZip);
        txn.setCardNum(cardNum);
        txn.setOrigTs(origTs);
        txn.setProcTs(procTs);
        return txn;
    }
}
