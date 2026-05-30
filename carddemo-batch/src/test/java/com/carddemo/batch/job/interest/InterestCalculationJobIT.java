package com.carddemo.batch.job.interest;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.DisclosureGroup;
import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.DisclosureGroupRepository;
import com.carddemo.common.repository.TranCatBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBatchTest
@SpringBootTest(classes = InterestCalculationJobIT.TestConfig.class)
@ActiveProfiles("dev")
class InterestCalculationJobIT {

    /** Loads only the interest calculation beans, avoiding the pre-existing
     *  transactionReader bean name conflict. */
    @SpringBootApplication
    @EntityScan(basePackages = "com.carddemo.common.entity")
    @EnableJpaRepositories(basePackages = "com.carddemo.common.repository")
    @ComponentScan(
            basePackages = "com.carddemo.batch.job.interest",
            useDefaultFilters = true
    )
    static class TestConfig {}

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job interestCalculationJob;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DisclosureGroupRepository disclosureGroupRepository;

    @Autowired
    private TranCatBalanceRepository tranCatBalanceRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(interestCalculationJob);
        tranCatBalanceRepository.deleteAll();
        disclosureGroupRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private JobExecution launchUniqueJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.nanoTime())
                .toJobParameters();
        return jobLauncherTestUtils.launchJob(params);
    }

    @Test
    void jobCompletesAndCalculatesInterest() throws Exception {
        Account acct = makeAccount(10000000001L, "10000.00", "250.00", "100.00", "GRP001");
        accountRepository.save(acct);

        DisclosureGroup dg = makeDisclosureGroup("GRP001", "SA", 1000, "18.00");
        disclosureGroupRepository.save(dg);

        TranCatBalance tcb = makeTcb(10000000001L, "SA", 1000, "6000.00");
        tranCatBalanceRepository.save(tcb);

        JobExecution execution = launchUniqueJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(ExitStatus.COMPLETED, execution.getExitStatus());

        Account updated = accountRepository.findById(10000000001L).orElseThrow();
        // interest = 6000 * 18 / 1200 = 90.00
        assertEquals(new BigDecimal("10090.00"), updated.getCurrBal());
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getCurrCycCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getCurrCycDebit()));
    }

    @Test
    void jobHandlesMultipleCategoriesPerAccount() throws Exception {
        Account acct = makeAccount(20000000001L, "5000.00", "80.00", "40.00", "GRP002");
        accountRepository.save(acct);

        disclosureGroupRepository.saveAll(List.of(
                makeDisclosureGroup("GRP002", "SA", 1000, "12.00"),
                makeDisclosureGroup("GRP002", "CR", 2000, "24.00")
        ));

        tranCatBalanceRepository.saveAll(List.of(
                makeTcb(20000000001L, "SA", 1000, "3000.00"),
                makeTcb(20000000001L, "CR", 2000, "1500.00")
        ));

        JobExecution execution = launchUniqueJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        Account updated = accountRepository.findById(20000000001L).orElseThrow();
        // int1 = 3000*12/1200 = 30.00, int2 = 1500*24/1200 = 30.00
        // total = 60, new bal = 5000 + 60 = 5060
        assertEquals(new BigDecimal("5060.00"), updated.getCurrBal());
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getCurrCycCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getCurrCycDebit()));
    }

    @Test
    void jobFallsBackToDefaultDisclosureGroup() throws Exception {
        Account acct = makeAccount(30000000001L, "2000.00", "0.00", "0.00", "UNKNOWN");
        accountRepository.save(acct);

        DisclosureGroup defaultDg = makeDisclosureGroup("DEFAULT", "SA", 1000, "6.00");
        disclosureGroupRepository.save(defaultDg);

        TranCatBalance tcb = makeTcb(30000000001L, "SA", 1000, "1200.00");
        tranCatBalanceRepository.save(tcb);

        JobExecution execution = launchUniqueJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        Account updated = accountRepository.findById(30000000001L).orElseThrow();
        // interest = 1200 * 6 / 1200 = 6.00
        assertEquals(new BigDecimal("2006.00"), updated.getCurrBal());
    }

    @Test
    void jobHandlesMultipleAccounts() throws Exception {
        Account acct1 = makeAccount(40000000001L, "1000.00", "20.00", "10.00", "GRP003");
        Account acct2 = makeAccount(40000000002L, "2000.00", "30.00", "15.00", "GRP003");
        accountRepository.saveAll(List.of(acct1, acct2));

        DisclosureGroup dg = makeDisclosureGroup("GRP003", "SA", 1000, "12.00");
        disclosureGroupRepository.save(dg);

        tranCatBalanceRepository.saveAll(List.of(
                makeTcb(40000000001L, "SA", 1000, "1200.00"),
                makeTcb(40000000002L, "SA", 1000, "2400.00")
        ));

        JobExecution execution = launchUniqueJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        Account u1 = accountRepository.findById(40000000001L).orElseThrow();
        Account u2 = accountRepository.findById(40000000002L).orElseThrow();

        // acct1: 1200*12/1200 = 12; bal = 1000+12 = 1012
        assertEquals(new BigDecimal("1012.00"), u1.getCurrBal());
        assertEquals(0, BigDecimal.ZERO.compareTo(u1.getCurrCycCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(u1.getCurrCycDebit()));

        // acct2: 2400*12/1200 = 24; bal = 2000+24 = 2024
        assertEquals(new BigDecimal("2024.00"), u2.getCurrBal());
        assertEquals(0, BigDecimal.ZERO.compareTo(u2.getCurrCycCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(u2.getCurrCycDebit()));
    }

    @Test
    void jobCompletesWithNoData() throws Exception {
        JobExecution execution = launchUniqueJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void jobHandlesZeroRateNoInterestAdded() throws Exception {
        Account acct = makeAccount(50000000001L, "3000.00", "75.00", "25.00", "GRP004");
        accountRepository.save(acct);

        DisclosureGroup dg = makeDisclosureGroup("GRP004", "SA", 1000, "0.00");
        disclosureGroupRepository.save(dg);

        TranCatBalance tcb = makeTcb(50000000001L, "SA", 1000, "10000.00");
        tranCatBalanceRepository.save(tcb);

        JobExecution execution = launchUniqueJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        Account updated = accountRepository.findById(50000000001L).orElseThrow();
        assertEquals(new BigDecimal("3000.00"), updated.getCurrBal());
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getCurrCycCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getCurrCycDebit()));
    }

    private Account makeAccount(Long acctId, String bal, String cycCredit,
                                String cycDebit, String groupId) {
        Account a = new Account();
        a.setAcctId(acctId);
        a.setCurrBal(new BigDecimal(bal));
        a.setCurrCycCredit(new BigDecimal(cycCredit));
        a.setCurrCycDebit(new BigDecimal(cycDebit));
        a.setGroupId(groupId);
        a.setActiveStatus("Y");
        return a;
    }

    private DisclosureGroup makeDisclosureGroup(String groupId, String typeCd,
                                                 int catCd, String rate) {
        DisclosureGroup dg = new DisclosureGroup();
        dg.setAcctGroupId(groupId);
        dg.setTypeCd(typeCd);
        dg.setCatCd(catCd);
        dg.setIntRate(new BigDecimal(rate));
        return dg;
    }

    private TranCatBalance makeTcb(Long acctId, String typeCd, int catCd, String balance) {
        TranCatBalance tcb = new TranCatBalance();
        tcb.setAcctId(acctId);
        tcb.setTypeCd(typeCd);
        tcb.setCatCd(catCd);
        tcb.setTranCatBal(new BigDecimal(balance));
        return tcb;
    }
}
