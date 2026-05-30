package com.carddemo.batch.statement;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.CustomerRepository;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest(classes = com.carddemo.batch.BatchApplication.class)
class StatementGenerationJobIT {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("statementGenerationJob")
    private Job statementGenerationJob;

    @Autowired
    private CardXrefRepository cardXrefRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(statementGenerationJob);
        transactionRepo.deleteAll();
        cardXrefRepo.deleteAll();
        accountRepo.deleteAll();
        customerRepo.deleteAll();
        loadTestData();
    }

    @Test
    void jobCompletes_andProducesStatementFile() throws Exception {
        String outputPath = tempDir.resolve("statements.txt").toString();
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2024-01-01")
                .addString("endDate", "2024-01-31")
                .addString("outputPath", outputPath)
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        List<String> lines = Files.readAllLines(Path.of(outputPath));
        assertTrue(lines.size() > 10, "Statement file should have content");

        long startCount = lines.stream().filter(l -> l.contains("START OF STATEMENT")).count();
        long endCount = lines.stream().filter(l -> l.contains("END OF STATEMENT")).count();
        assertEquals(2, startCount, "Should have 2 statements (one per card xref)");
        assertEquals(2, endCount);

        assertTrue(lines.stream().anyMatch(l -> l.contains("John M Doe")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("123 Main St")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Account ID")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("5000.00")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("10000.00")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("750")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Grocery purchase")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Gas station")));

        assertTrue(lines.stream().anyMatch(l -> l.contains("Jane Smith")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("456 Oak Ave")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Return refund")));

        assertTrue(lines.stream().anyMatch(l -> l.contains("TRANSACTION SUMMARY")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Total Debits:")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Total Credits:")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("New Balance:")));

        for (String line : lines) {
            if (!line.isBlank()) {
                assertEquals(80, line.length(),
                        "Non-blank lines must be 80 chars: '" + line + "'");
            }
        }
    }

    @Test
    void jobWithNoMatchingTransactions_completesSuccessfully() throws Exception {
        String outputPath = tempDir.resolve("empty-stmt.txt").toString();
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2025-06-01")
                .addString("endDate", "2025-06-30")
                .addString("outputPath", outputPath)
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        List<String> lines = Files.readAllLines(Path.of(outputPath));
        long startCount = lines.stream().filter(l -> l.contains("START OF STATEMENT")).count();
        assertEquals(2, startCount, "Statements still generated, just with no transactions");
    }

    @Test
    void jobFilters_byDateRange() throws Exception {
        String outputPath = tempDir.resolve("filtered-stmt.txt").toString();
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2024-01-15")
                .addString("endDate", "2024-01-15")
                .addString("outputPath", outputPath)
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        String content = Files.readString(Path.of(outputPath));
        assertTrue(content.contains("TXN0000000000001"), "Should include Jan 15 txn");
        assertTrue(content.contains("TXN0000000000003"), "Should include Jan 15 return");
    }

    private void loadTestData() {
        Customer cust1 = new Customer();
        cust1.setCustId(1001L);
        cust1.setFirstName("John");
        cust1.setMiddleName("M");
        cust1.setLastName("Doe");
        cust1.setAddrLine1("123 Main St");
        cust1.setAddrLine2("Apt 4");
        cust1.setAddrLine3("Seattle");
        cust1.setStateCode("WA");
        cust1.setCountryCode("US");
        cust1.setZip("98101");
        cust1.setFicoCreditScore(750);
        customerRepo.save(cust1);

        Customer cust2 = new Customer();
        cust2.setCustId(1002L);
        cust2.setFirstName("Jane");
        cust2.setLastName("Smith");
        cust2.setAddrLine1("456 Oak Ave");
        cust2.setAddrLine3("Portland");
        cust2.setStateCode("OR");
        cust2.setCountryCode("US");
        cust2.setZip("97201");
        cust2.setFicoCreditScore(680);
        customerRepo.save(cust2);

        Account acct1 = new Account();
        acct1.setAcctId(10001L);
        acct1.setActiveStatus("Y");
        acct1.setCurrBal(new BigDecimal("5000.00"));
        acct1.setCreditLimit(new BigDecimal("10000.00"));
        accountRepo.save(acct1);

        Account acct2 = new Account();
        acct2.setAcctId(10002L);
        acct2.setActiveStatus("Y");
        acct2.setCurrBal(new BigDecimal("2500.00"));
        acct2.setCreditLimit(new BigDecimal("8000.00"));
        accountRepo.save(acct2);

        CardXref xref1 = new CardXref();
        xref1.setXrefCardNum("1111111111111111");
        xref1.setCustId(1001L);
        xref1.setAcctId(10001L);
        cardXrefRepo.save(xref1);

        CardXref xref2 = new CardXref();
        xref2.setXrefCardNum("2222222222222222");
        xref2.setCustId(1002L);
        xref2.setAcctId(10002L);
        cardXrefRepo.save(xref2);

        Transaction tx1 = new Transaction();
        tx1.setTranId("TXN0000000000001");
        tx1.setTypeCd("SA");
        tx1.setDesc("Grocery purchase");
        tx1.setAmt(new BigDecimal("45.50"));
        tx1.setCardNum("1111111111111111");
        tx1.setOrigTs("2024-01-15-10.00.00.000000");
        tx1.setProcTs("2024-01-15-10.00.00.000000");
        transactionRepo.save(tx1);

        Transaction tx2 = new Transaction();
        tx2.setTranId("TXN0000000000002");
        tx2.setTypeCd("SA");
        tx2.setDesc("Gas station");
        tx2.setAmt(new BigDecimal("30.00"));
        tx2.setCardNum("1111111111111111");
        tx2.setOrigTs("2024-01-16-10.00.00.000000");
        tx2.setProcTs("2024-01-16-10.00.00.000000");
        transactionRepo.save(tx2);

        Transaction tx3 = new Transaction();
        tx3.setTranId("TXN0000000000003");
        tx3.setTypeCd("RT");
        tx3.setDesc("Return refund");
        tx3.setAmt(new BigDecimal("-25.00"));
        tx3.setCardNum("2222222222222222");
        tx3.setOrigTs("2024-01-15-14.00.00.000000");
        tx3.setProcTs("2024-01-15-14.00.00.000000");
        transactionRepo.save(tx3);
    }
}
