package com.carddemo.batch.posting;


import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.DailyTransaction;
import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.entity.TranCatBalanceId;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.DailyTransactionRepository;
import com.carddemo.common.repository.TranCatBalanceRepository;
import com.carddemo.common.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for TransactionPostingJob (CBTRN02C).
 * Seeds daily transactions including overlimit and expired-card scenarios,
 * runs the job, then verifies all 6 business rules.
 */
@SpringBootTest(classes = PostingTestApplication.class)
class TransactionPostingJobIT {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("batch.posting.reject-path",
                () -> tempDir.resolve("rejected.dat").toString());
    }

    @Autowired private JobLauncher jobLauncher;
    @Autowired @Qualifier("transactionPostingJob") private Job transactionPostingJob;

    @Autowired private DailyTransactionRepository dailyTransactionRepo;
    @Autowired private TransactionRepository transactionRepo;
    @Autowired private AccountRepository accountRepo;
    @Autowired private CardXrefRepository cardXrefRepo;
    @Autowired private TranCatBalanceRepository tranCatBalanceRepo;

    @BeforeEach
    void cleanAndSeed() {
        tranCatBalanceRepo.deleteAll();
        transactionRepo.deleteAll();
        dailyTransactionRepo.deleteAll();
        cardXrefRepo.deleteAll();
        accountRepo.deleteAll();

        // --- Accounts ---
        accountRepo.save(account(11111111111L, "Y", "5000.00", "10000.00", "2025-12-31"));
        accountRepo.save(account(22222222222L, "Y", "2000.00", "3000.00", "2023-06-30")); // expired
        accountRepo.save(account(33333333333L, "Y", "9800.00", "10000.00", "2025-12-31")); // near-limit

        // --- XREF ---
        cardXrefRepo.save(cardXref("4111111111111111", 1000001L, 11111111111L));
        cardXrefRepo.save(cardXref("4222222222222222", 1000002L, 22222222222L));
        cardXrefRepo.save(cardXref("4333333333333333", 1000003L, 33333333333L));

        // --- Daily transactions ---
        // 1) Valid: normal posting
        dailyTransactionRepo.save(dailyTran("TRN0000000000001", "4111111111111111",
                "SA", 5001, "150.00", "2024-06-01 10:00:00.000"));
        // 2) Valid: credit (negative amount)
        dailyTransactionRepo.save(dailyTran("TRN0000000000002", "4111111111111111",
                "SA", 5001, "-50.00", "2024-06-01 11:00:00.000"));
        // 3) Valid: different category
        dailyTransactionRepo.save(dailyTran("TRN0000000000003", "4111111111111111",
                "PR", 6001, "200.00", "2024-06-01 12:00:00.000"));
        // 4) Reject 100: invalid card number
        dailyTransactionRepo.save(dailyTran("TRN0000000000004", "9999999999999999",
                "SA", 5001, "10.00", "2024-06-01 13:00:00.000"));
        // 5) Reject 103: expired card (acct 22222222222 expired 2023-06-30)
        dailyTransactionRepo.save(dailyTran("TRN0000000000005", "4222222222222222",
                "SA", 5001, "25.00", "2024-06-01 14:00:00.000"));
        // 6) Reject 102: overlimit (acct 33333333333: cycCredit=0, cycDebit=0,
        //    creditLimit=10000, bal=9800 but tempBal = 0 - 0 + 300 = 300 ≤ 10000)
        //    Actually let's set cycCredit to be high to trigger overlimit:
        //    We need cycCredit - cycDebit + amt > creditLimit
        //    → set cycCredit=9800, amt=300 → 9800 - 0 + 300 = 10100 > 10000
        dailyTransactionRepo.save(dailyTran("TRN0000000000006", "4333333333333333",
                "SA", 5001, "300.00", "2024-06-01 15:00:00.000"));

        // Set cycCredit for acct 33333333333 to trigger overlimit
        Account overlimitAcct = accountRepo.findById(33333333333L).orElseThrow();
        overlimitAcct.setCurrCycCredit(new BigDecimal("9800.00"));
        overlimitAcct.setCurrCycDebit(BigDecimal.ZERO);
        accountRepo.save(overlimitAcct);
    }

    @Test
    void jobCompletesAndVerifiesAllBusinessRules() throws Exception {
        // Record prior state
        BigDecimal priorBal11 = accountRepo.findById(11111111111L).orElseThrow().getCurrBal();

        // Run job
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(transactionPostingJob, params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus(), "Job should complete");

        // ---- Rule 1: Record count reconciliation ----
        // 6 input: 3 accepted + 3 rejected
        long totalInput = 6;
        List<Transaction> postedTxns = transactionRepo.findAll();
        assertEquals(3, postedTxns.size(), "3 transactions should be posted");

        Path rejectFile = tempDir.resolve("rejected.dat");
        assertTrue(Files.exists(rejectFile), "Reject file should exist");
        List<String> rejectLines = Files.readAllLines(rejectFile);
        assertEquals(3, rejectLines.size(), "3 transactions should be rejected");
        assertEquals(totalInput, postedTxns.size() + rejectLines.size(),
                "Rule 1: input = processed + rejected");

        // ---- Rule 2: Balance integrity ----
        // Account 11111111111: prior=5000, posted amounts = 150 + (-50) + 200 = 300
        Account acct11 = accountRepo.findById(11111111111L).orElseThrow();
        BigDecimal expectedBal = priorBal11.add(new BigDecimal("300.00"));
        assertEquals(0, expectedBal.compareTo(acct11.getCurrBal()),
                "Rule 2: new balance = prior + SUM(posted)");

        // ---- Rule 3: Cycle credit/debit split ----
        // Credits (>=0): 150 + 200 = 350
        // Debits (<0): -50
        assertEquals(0, new BigDecimal("350.00").compareTo(acct11.getCurrCycCredit()),
                "Rule 3: cycle credit = SUM(positive posted amounts)");
        assertEquals(0, new BigDecimal("-50.00").compareTo(acct11.getCurrCycDebit()),
                "Rule 3: cycle debit = SUM(negative posted amounts)");

        // ---- Rule 4: TCATBAL category balance ----
        // For acct 11111111111, SA/5001: 150 + (-50) = 100
        Optional<TranCatBalance> tcb1 = tranCatBalanceRepo.findById(
                new TranCatBalanceId(11111111111L, "SA", 5001));
        assertTrue(tcb1.isPresent(), "TCATBAL for SA/5001 should exist");
        assertEquals(0, new BigDecimal("100.00").compareTo(tcb1.get().getTranCatBal()),
                "Rule 4: category balance = SUM(matching trans amounts)");

        // For acct 11111111111, PR/6001: 200
        Optional<TranCatBalance> tcb2 = tranCatBalanceRepo.findById(
                new TranCatBalanceId(11111111111L, "PR", 6001));
        assertTrue(tcb2.isPresent(), "TCATBAL for PR/6001 should exist");
        assertEquals(0, new BigDecimal("200.00").compareTo(tcb2.get().getTranCatBal()),
                "Rule 4: category balance for PR/6001");

        // ---- Rule 5: Credit limit rejection (reason 102) ----
        assertTrue(rejectLines.stream().anyMatch(l -> l.contains("|0102|")),
                "Rule 5: overlimit rejection (reason 102) should be in reject file");
        // Account 33333333333 should NOT have been updated
        Account acct33 = accountRepo.findById(33333333333L).orElseThrow();
        assertEquals(0, new BigDecimal("9800.00").compareTo(acct33.getCurrBal()),
                "Overlimit account balance should be unchanged");

        // ---- Rule 6: Expiration rejection (reason 103) ----
        assertTrue(rejectLines.stream().anyMatch(l -> l.contains("|0103|")),
                "Rule 6: expiration rejection (reason 103) should be in reject file");
        // Account 22222222222 should NOT have been updated
        Account acct22 = accountRepo.findById(22222222222L).orElseThrow();
        assertEquals(0, new BigDecimal("2000.00").compareTo(acct22.getCurrBal()),
                "Expired account balance should be unchanged");

        // Verify reason 100 (invalid card) is also present
        assertTrue(rejectLines.stream().anyMatch(l -> l.contains("|0100|")),
                "Invalid card rejection (reason 100) should be in reject file");
    }

    @Test
    void jobWithNoData_completesEmpty() throws Exception {
        dailyTransactionRepo.deleteAll();

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis() + 1)
                .toJobParameters();
        JobExecution execution = jobLauncher.run(transactionPostingJob, params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(0, transactionRepo.count());
    }

    // ---- data builders ----

    private Account account(Long id, String status, String bal, String limit, String expDate) {
        Account a = new Account();
        a.setAcctId(id);
        a.setActiveStatus(status);
        a.setCurrBal(new BigDecimal(bal));
        a.setCreditLimit(new BigDecimal(limit));
        a.setCashCreditLimit(new BigDecimal("2000.00"));
        a.setOpenDate("2020-01-01");
        a.setExpirationDate(expDate);
        a.setCurrCycCredit(BigDecimal.ZERO);
        a.setCurrCycDebit(BigDecimal.ZERO);
        return a;
    }

    private CardXref cardXref(String cardNum, Long custId, Long acctId) {
        CardXref x = new CardXref();
        x.setXrefCardNum(cardNum);
        x.setCustId(custId);
        x.setAcctId(acctId);
        return x;
    }

    private DailyTransaction dailyTran(String tranId, String cardNum,
                                        String typeCd, int catCd,
                                        String amt, String origTs) {
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId(tranId);
        dt.setCardNum(cardNum);
        dt.setOrigTs(origTs);
        dt.setTypeCd(typeCd);
        dt.setCatCd(catCd);
        dt.setSource("POS");
        dt.setDesc("Test transaction");
        dt.setAmt(new BigDecimal(amt));
        dt.setMerchantId(900001L);
        dt.setMerchantName("Test Merchant");
        dt.setMerchantCity("New York");
        dt.setMerchantZip("10001");
        return dt;
    }
}
