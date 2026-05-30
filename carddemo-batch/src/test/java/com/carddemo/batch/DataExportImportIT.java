package com.carddemo.batch;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.CustomerRepository;
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
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trip integration test: export data from H2, clear all tables,
 * import from export file, verify record counts and sample records match.
 */
@SpringBootTest
class DataExportImportIT {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void overrideExportPath(DynamicPropertyRegistry registry) {
        registry.add("carddemo.export.file-path",
                () -> tempDir.resolve("roundtrip-export.dat").toString());
    }

    @Autowired private JobLauncher jobLauncher;
    @Autowired @Qualifier("dataExportJob") private Job dataExportJob;
    @Autowired @Qualifier("dataImportJob") private Job dataImportJob;

    @Autowired private CustomerRepository customerRepo;
    @Autowired private AccountRepository accountRepo;
    @Autowired private CardXrefRepository cardXrefRepo;
    @Autowired private TransactionRepository transactionRepo;
    @Autowired private CardRepository cardRepo;
    @Autowired private TranCatBalanceRepository tranCatBalanceRepo;

    @BeforeEach
    void cleanTables() {
        tranCatBalanceRepo.deleteAll();
        transactionRepo.deleteAll();
        cardXrefRepo.deleteAll();
        cardRepo.deleteAll();
        accountRepo.deleteAll();
        customerRepo.deleteAll();
    }

    @Test
    void roundTrip_exportThenImport_shouldPreserveData() throws Exception {
        // --- Setup: insert test data ---
        Customer cust1 = customer(1000001L, "Alice", "B", "Smith");
        Customer cust2 = customer(1000002L, "Bob", null, "Jones");
        customerRepo.saveAll(List.of(cust1, cust2));

        Account acct1 = account(11111111111L, "Y", "5000.00", "10000.00");
        Account acct2 = account(22222222222L, "N", "0.00", "5000.00");
        accountRepo.saveAll(List.of(acct1, acct2));

        CardXref xref1 = cardXref("4111111111111111", 1000001L, 11111111111L);
        CardXref xref2 = cardXref("4222222222222222", 1000002L, 22222222222L);
        cardXrefRepo.saveAll(List.of(xref1, xref2));

        Transaction txn1 = transaction("TRN0000000000001", "SA", 5001, "4.50");
        transactionRepo.save(txn1);

        Card card1 = card("4111111111111111", 11111111111L, 123, "ALICE SMITH");
        Card card2 = card("4222222222222222", 22222222222L, 456, "BOB JONES");
        cardRepo.saveAll(List.of(card1, card2));

        TranCatBalance bal1 = tranCatBalance(11111111111L, "SA", 5001, "1500.00");
        tranCatBalanceRepo.save(bal1);

        // Record original counts
        long origCustomers = customerRepo.count();
        long origAccounts = accountRepo.count();
        long origXrefs = cardXrefRepo.count();
        long origTransactions = transactionRepo.count();
        long origCards = cardRepo.count();
        long origBalances = tranCatBalanceRepo.count();

        // --- Export ---
        JobParameters exportParams = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution exportExec = jobLauncher.run(dataExportJob, exportParams);
        assertEquals(BatchStatus.COMPLETED, exportExec.getStatus(),
                "Export job should complete successfully");

        // Verify export file was created
        assertTrue(tempDir.resolve("roundtrip-export.dat").toFile().exists(),
                "Export file should exist");

        // --- Clear all tables ---
        tranCatBalanceRepo.deleteAll();
        transactionRepo.deleteAll();
        cardXrefRepo.deleteAll();
        cardRepo.deleteAll();
        accountRepo.deleteAll();
        customerRepo.deleteAll();

        assertEquals(0, customerRepo.count());
        assertEquals(0, accountRepo.count());
        assertEquals(0, cardXrefRepo.count());
        assertEquals(0, transactionRepo.count());
        assertEquals(0, cardRepo.count());
        assertEquals(0, tranCatBalanceRepo.count());

        // --- Import ---
        JobParameters importParams = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis() + 1)
                .toJobParameters();
        JobExecution importExec = jobLauncher.run(dataImportJob, importParams);
        assertEquals(BatchStatus.COMPLETED, importExec.getStatus(),
                "Import job should complete successfully");

        // --- Verify record counts match ---
        assertEquals(origCustomers, customerRepo.count(), "Customer count mismatch");
        assertEquals(origAccounts, accountRepo.count(), "Account count mismatch");
        assertEquals(origXrefs, cardXrefRepo.count(), "CardXref count mismatch");
        assertEquals(origTransactions, transactionRepo.count(), "Transaction count mismatch");
        assertEquals(origCards, cardRepo.count(), "Card count mismatch");
        assertEquals(origBalances, tranCatBalanceRepo.count(), "TranCatBalance count mismatch");

        // --- Verify sample records ---
        Customer importedCust = customerRepo.findById(1000001L).orElseThrow();
        assertEquals("Alice", importedCust.getFirstName());
        assertEquals("Smith", importedCust.getLastName());
        assertEquals("B", importedCust.getMiddleName());

        Customer importedCust2 = customerRepo.findById(1000002L).orElseThrow();
        assertEquals("Bob", importedCust2.getFirstName());

        Account importedAcct = accountRepo.findById(11111111111L).orElseThrow();
        assertEquals("Y", importedAcct.getActiveStatus());
        assertEquals(0, new BigDecimal("5000.00").compareTo(importedAcct.getCurrBal()));

        CardXref importedXref = cardXrefRepo.findById("4111111111111111").orElseThrow();
        assertEquals(1000001L, importedXref.getCustId());

        Transaction importedTxn = transactionRepo.findById("TRN0000000000001").orElseThrow();
        assertEquals("SA", importedTxn.getTypeCd());
        assertEquals(0, new BigDecimal("4.50").compareTo(importedTxn.getAmt()));

        Card importedCard = cardRepo.findById("4111111111111111").orElseThrow();
        assertEquals(11111111111L, importedCard.getAcctId());
        assertEquals(123, importedCard.getCvvCd());

        TranCatBalance importedBal = tranCatBalanceRepo.findAll().get(0);
        assertEquals(11111111111L, importedBal.getAcctId());
        assertEquals(0, new BigDecimal("1500.00").compareTo(importedBal.getTranCatBal()));
    }

    // ---- Test data builders ----

    private Customer customer(Long id, String first, String middle, String last) {
        Customer c = new Customer();
        c.setCustId(id);
        c.setFirstName(first);
        c.setMiddleName(middle);
        c.setLastName(last);
        c.setStateCode("NY");
        c.setCountryCode("US");
        c.setZip("10001");
        c.setSsn(100000000L + id);
        c.setFicoCreditScore(750);
        return c;
    }

    private Account account(Long id, String status, String bal, String limit) {
        Account a = new Account();
        a.setAcctId(id);
        a.setActiveStatus(status);
        a.setCurrBal(new BigDecimal(bal));
        a.setCreditLimit(new BigDecimal(limit));
        a.setCashCreditLimit(new BigDecimal("2000.00"));
        a.setOpenDate("2020-01-01");
        a.setExpirationDate("2025-12-31");
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

    private Transaction transaction(String tranId, String typeCd, int catCd, String amt) {
        Transaction t = new Transaction();
        t.setTranId(tranId);
        t.setTypeCd(typeCd);
        t.setCatCd(catCd);
        t.setSource("POS");
        t.setDesc("Test transaction");
        t.setAmt(new BigDecimal(amt));
        t.setMerchantId(900001L);
        t.setMerchantName("Test Merchant");
        t.setMerchantCity("New York");
        t.setMerchantZip("10001");
        t.setCardNum("4111111111111111");
        t.setOrigTs("2024-01-15 10:30:00.000");
        t.setProcTs("2024-01-15 10:30:01.000");
        return t;
    }

    private Card card(String cardNum, Long acctId, int cvv, String name) {
        Card d = new Card();
        d.setCardNum(cardNum);
        d.setAcctId(acctId);
        d.setCvvCd(cvv);
        d.setEmbossedName(name);
        d.setExpirationDate("2025-12-31");
        d.setActiveStatus("Y");
        return d;
    }

    private TranCatBalance tranCatBalance(Long acctId, String typeCd, int catCd, String bal) {
        TranCatBalance b = new TranCatBalance();
        b.setAcctId(acctId);
        b.setTypeCd(typeCd);
        b.setCatCd(catCd);
        b.setTranCatBal(new BigDecimal(bal));
        return b;
    }
}
