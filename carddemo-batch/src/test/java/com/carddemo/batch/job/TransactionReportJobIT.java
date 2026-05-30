package com.carddemo.batch.job;

import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.TranCategory;
import com.carddemo.common.entity.TranType;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TranCategoryRepository;
import com.carddemo.common.repository.TranTypeRepository;
import com.carddemo.common.repository.TransactionRepository;
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
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest(classes = com.carddemo.batch.BatchApplication.class)
class TransactionReportJobIT {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job transactionReportJob;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private CardXrefRepository cardXrefRepo;

    @Autowired
    private TranTypeRepository tranTypeRepo;

    @Autowired
    private TranCategoryRepository tranCatRepo;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(transactionReportJob);
        transactionRepo.deleteAll();
        cardXrefRepo.deleteAll();
        tranTypeRepo.deleteAll();
        tranCatRepo.deleteAll();
        loadTestData();
    }

    @Test
    void jobCompletes_andProducesReport() throws Exception {
        String outputPath = tempDir.resolve("report.txt").toString();
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2024-01-01")
                .addString("endDate", "2024-01-31")
                .addString("outputPath", outputPath)
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        List<String> lines = Files.readAllLines(Path.of(outputPath));
        assertTrue(lines.size() > 5, "Report should have at least header + details + totals");

        assertTrue(lines.get(0).contains("DALYREPT"));
        assertTrue(lines.get(0).contains("Daily Transaction Report"));
        assertTrue(lines.get(0).contains("2024-01-01"));
        assertTrue(lines.get(0).contains("2024-01-31"));

        assertTrue(lines.get(1).isBlank());

        assertTrue(lines.get(2).contains("Transaction ID"));
        assertTrue(lines.get(2).contains("Account ID"));
        assertTrue(lines.get(2).contains("Amount"));

        assertEquals("-".repeat(133), lines.get(3));

        boolean hasDetail1 = lines.stream().anyMatch(l -> l.contains("TXN0000000000001"));
        assertTrue(hasDetail1, "Should contain first transaction");

        boolean hasDetail3 = lines.stream().anyMatch(l -> l.contains("TXN0000000000003"));
        assertTrue(hasDetail3, "Should contain third transaction");

        boolean hasAccountTotal = lines.stream().anyMatch(l -> l.contains("Account Total"));
        assertTrue(hasAccountTotal, "Should contain account totals");

        boolean hasPageTotal = lines.stream().anyMatch(l -> l.contains("Page Total"));
        assertTrue(hasPageTotal, "Should contain page total");

        boolean hasGrandTotal = lines.stream().anyMatch(l -> l.contains("Grand Total"));
        assertTrue(hasGrandTotal, "Should contain grand total");

        for (String line : lines) {
            assertEquals(133, line.length(), "Every line must be 133 chars: '"
                    + line.substring(0, Math.min(40, line.length())) + "...'");
        }
    }

    @Test
    void jobFilters_byDateRange() throws Exception {
        String outputPath = tempDir.resolve("filtered-report.txt").toString();
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2024-01-15")
                .addString("endDate", "2024-01-15")
                .addString("outputPath", outputPath)
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        String content = Files.readString(Path.of(outputPath));
        assertTrue(content.contains("TXN0000000000001"), "Should include Jan 15 transaction");
        assertTrue(content.contains("TXN0000000000003"), "Should include Jan 15 transaction");
    }

    @Test
    void jobWithNoMatchingTransactions_producesEmptyReport() throws Exception {
        String outputPath = tempDir.resolve("empty-report.txt").toString();
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2025-01-01")
                .addString("endDate", "2025-01-31")
                .addString("outputPath", outputPath)
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        List<String> lines = Files.readAllLines(Path.of(outputPath));
        assertTrue(lines.isEmpty(), "No matching transactions should produce empty report");
    }

    private void loadTestData() {
        TranType sale = new TranType();
        sale.setTranType("SA");
        sale.setTranTypeDesc("Sale");
        tranTypeRepo.save(sale);

        TranType ret = new TranType();
        ret.setTranType("RT");
        ret.setTranTypeDesc("Return");
        tranTypeRepo.save(ret);

        TranCategory cat1 = new TranCategory();
        cat1.setTypeCd("SA");
        cat1.setCatCd(5001);
        cat1.setTranCatTypeDesc("Online Purchase");
        tranCatRepo.save(cat1);

        TranCategory cat2 = new TranCategory();
        cat2.setTypeCd("RT");
        cat2.setCatCd(5002);
        cat2.setTranCatTypeDesc("Merchandise Return");
        tranCatRepo.save(cat2);

        CardXref xref1 = new CardXref();
        xref1.setXrefCardNum("1111111111111111");
        xref1.setCustId(1001L);
        xref1.setAcctId(1L);
        cardXrefRepo.save(xref1);

        CardXref xref2 = new CardXref();
        xref2.setXrefCardNum("2222222222222222");
        xref2.setCustId(1002L);
        xref2.setAcctId(2L);
        cardXrefRepo.save(xref2);

        Transaction tx1 = new Transaction();
        tx1.setTranId("TXN0000000000001");
        tx1.setTypeCd("SA");
        tx1.setCatCd(5001);
        tx1.setSource("ONLINE");
        tx1.setDesc("Test purchase 1");
        tx1.setAmt(new BigDecimal("100.00"));
        tx1.setCardNum("1111111111111111");
        tx1.setOrigTs("2024-01-15-10.00.00.000000");
        tx1.setProcTs("2024-01-15-10.00.00.000000");
        transactionRepo.save(tx1);

        Transaction tx2 = new Transaction();
        tx2.setTranId("TXN0000000000002");
        tx2.setTypeCd("SA");
        tx2.setCatCd(5001);
        tx2.setSource("ONLINE");
        tx2.setDesc("Test purchase 2");
        tx2.setAmt(new BigDecimal("200.50"));
        tx2.setCardNum("1111111111111111");
        tx2.setOrigTs("2024-01-16-10.00.00.000000");
        tx2.setProcTs("2024-01-16-10.00.00.000000");
        transactionRepo.save(tx2);

        Transaction tx3 = new Transaction();
        tx3.setTranId("TXN0000000000003");
        tx3.setTypeCd("RT");
        tx3.setCatCd(5002);
        tx3.setSource("POS");
        tx3.setDesc("Test return");
        tx3.setAmt(new BigDecimal("-50.00"));
        tx3.setCardNum("2222222222222222");
        tx3.setOrigTs("2024-01-15-14.00.00.000000");
        tx3.setProcTs("2024-01-15-14.00.00.000000");
        transactionRepo.save(tx3);
    }
}
