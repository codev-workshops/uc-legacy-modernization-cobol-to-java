package com.carddemo.batch.job.interest;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.DisclosureGroup;
import com.carddemo.common.entity.DisclosureGroupId;
import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.DisclosureGroupRepository;
import com.carddemo.common.repository.TranCatBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestCalculationTaskletTest {

    @Mock private TranCatBalanceRepository tranCatBalanceRepository;
    @Mock private DisclosureGroupRepository disclosureGroupRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private StepContribution stepContribution;
    @Mock private ChunkContext chunkContext;

    private InterestCalculationTasklet tasklet;

    @BeforeEach
    void setUp() {
        tasklet = new InterestCalculationTasklet(
                tranCatBalanceRepository, disclosureGroupRepository, accountRepository);
    }

    @Test
    void computeMonthlyInterest_normalCase() {
        BigDecimal balance = new BigDecimal("1000.00");
        BigDecimal rate = new BigDecimal("18.00");
        BigDecimal result = tasklet.computeMonthlyInterest(balance, rate);
        assertEquals(new BigDecimal("15.00"), result);
    }

    @Test
    void computeMonthlyInterest_zeroBalance() {
        BigDecimal result = tasklet.computeMonthlyInterest(BigDecimal.ZERO, new BigDecimal("18.00"));
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void computeMonthlyInterest_zeroRate() {
        BigDecimal result = tasklet.computeMonthlyInterest(new BigDecimal("5000.00"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void computeMonthlyInterest_negativeBalance() {
        BigDecimal balance = new BigDecimal("-500.00");
        BigDecimal rate = new BigDecimal("12.00");
        BigDecimal result = tasklet.computeMonthlyInterest(balance, rate);
        assertEquals(new BigDecimal("-5.00"), result);
    }

    @Test
    void computeMonthlyInterest_nullBalance() {
        BigDecimal result = tasklet.computeMonthlyInterest(null, new BigDecimal("18.00"));
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void computeMonthlyInterest_nullRate() {
        BigDecimal result = tasklet.computeMonthlyInterest(new BigDecimal("1000.00"), null);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void computeMonthlyInterest_fractionalResult() {
        BigDecimal balance = new BigDecimal("1000.00");
        BigDecimal rate = new BigDecimal("19.99");
        BigDecimal result = tasklet.computeMonthlyInterest(balance, rate);
        // 1000 * 19.99 / 1200 = 16.6583... → rounds to 16.66
        assertEquals(new BigDecimal("16.66"), result);
    }

    @Test
    void getInterestRate_found() {
        DisclosureGroup dg = new DisclosureGroup();
        dg.setIntRate(new BigDecimal("18.00"));
        when(disclosureGroupRepository.findById(new DisclosureGroupId("GRP1", "SA", 1000)))
                .thenReturn(Optional.of(dg));

        BigDecimal rate = tasklet.getInterestRate("GRP1", "SA", 1000);
        assertEquals(new BigDecimal("18.00"), rate);
    }

    @Test
    void getInterestRate_fallbackToDefault() {
        when(disclosureGroupRepository.findById(new DisclosureGroupId("GRP1", "SA", 1000)))
                .thenReturn(Optional.empty());
        DisclosureGroup defaultDg = new DisclosureGroup();
        defaultDg.setIntRate(new BigDecimal("12.00"));
        when(disclosureGroupRepository.findById(new DisclosureGroupId("DEFAULT", "SA", 1000)))
                .thenReturn(Optional.of(defaultDg));

        BigDecimal rate = tasklet.getInterestRate("GRP1", "SA", 1000);
        assertEquals(new BigDecimal("12.00"), rate);
    }

    @Test
    void getInterestRate_noDefaultEither() {
        when(disclosureGroupRepository.findById(any())).thenReturn(Optional.empty());

        BigDecimal rate = tasklet.getInterestRate("GRP1", "SA", 1000);
        assertEquals(BigDecimal.ZERO, rate);
    }

    @Test
    void getInterestRate_nullGroupId() {
        BigDecimal rate = tasklet.getInterestRate(null, "SA", 1000);
        assertEquals(BigDecimal.ZERO, rate);
    }

    @Test
    void execute_singleAccountSingleBalance() throws Exception {
        TranCatBalance tcb = makeTcb(1001L, "SA", 1000, "5000.00");
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(tcb));

        Account acct = makeAccount(1001L, "1000.00", "100.00", "50.00", "GRP1");
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(acct));

        DisclosureGroup dg = new DisclosureGroup();
        dg.setIntRate(new BigDecimal("18.00"));
        when(disclosureGroupRepository.findById(new DisclosureGroupId("GRP1", "SA", 1000)))
                .thenReturn(Optional.of(dg));

        tasklet.execute(stepContribution, chunkContext);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());

        Account saved = captor.getValue();
        // interest = 5000 * 18 / 1200 = 75.00; new balance = 1000 + 75 = 1075
        assertEquals(new BigDecimal("1075.00"), saved.getCurrBal());
        assertEquals(BigDecimal.ZERO, saved.getCurrCycCredit());
        assertEquals(BigDecimal.ZERO, saved.getCurrCycDebit());
    }

    @Test
    void execute_multipleBalancesForSameAccount() throws Exception {
        TranCatBalance tcb1 = makeTcb(1001L, "SA", 1000, "3000.00");
        TranCatBalance tcb2 = makeTcb(1001L, "CR", 2000, "2000.00");
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(tcb1, tcb2));

        Account acct = makeAccount(1001L, "500.00", "10.00", "20.00", "GRP1");
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(acct));

        DisclosureGroup dg1 = new DisclosureGroup();
        dg1.setIntRate(new BigDecimal("12.00"));
        when(disclosureGroupRepository.findById(new DisclosureGroupId("GRP1", "SA", 1000)))
                .thenReturn(Optional.of(dg1));
        DisclosureGroup dg2 = new DisclosureGroup();
        dg2.setIntRate(new BigDecimal("24.00"));
        when(disclosureGroupRepository.findById(new DisclosureGroupId("GRP1", "CR", 2000)))
                .thenReturn(Optional.of(dg2));

        tasklet.execute(stepContribution, chunkContext);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(captor.capture());

        Account saved = captor.getValue();
        // int1 = 3000*12/1200 = 30.00, int2 = 2000*24/1200 = 40.00
        // total = 70.00, new bal = 500 + 70 = 570
        assertEquals(new BigDecimal("570.00"), saved.getCurrBal());
        assertEquals(BigDecimal.ZERO, saved.getCurrCycCredit());
        assertEquals(BigDecimal.ZERO, saved.getCurrCycDebit());
    }

    @Test
    void execute_multipleAccounts() throws Exception {
        TranCatBalance tcb1 = makeTcb(1001L, "SA", 1000, "1200.00");
        TranCatBalance tcb2 = makeTcb(1002L, "SA", 1000, "2400.00");
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(tcb1, tcb2));

        Account acct1 = makeAccount(1001L, "100.00", "5.00", "3.00", "GRP1");
        Account acct2 = makeAccount(1002L, "200.00", "10.00", "7.00", "GRP1");
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(acct1));
        when(accountRepository.findById(1002L)).thenReturn(Optional.of(acct2));

        DisclosureGroup dg = new DisclosureGroup();
        dg.setIntRate(new BigDecimal("12.00"));
        when(disclosureGroupRepository.findById(new DisclosureGroupId("GRP1", "SA", 1000)))
                .thenReturn(Optional.of(dg));

        tasklet.execute(stepContribution, chunkContext);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(2)).save(captor.capture());

        List<Account> saved = captor.getAllValues();
        // acct1: 1200*12/1200=12; bal=100+12=112
        assertEquals(new BigDecimal("112.00"), saved.get(0).getCurrBal());
        assertEquals(BigDecimal.ZERO, saved.get(0).getCurrCycCredit());
        // acct2: 2400*12/1200=24; bal=200+24=224
        assertEquals(new BigDecimal("224.00"), saved.get(1).getCurrBal());
        assertEquals(BigDecimal.ZERO, saved.get(1).getCurrCycDebit());
    }

    @Test
    void execute_noBalances() throws Exception {
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of());

        tasklet.execute(stepContribution, chunkContext);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void execute_accountNotFound() throws Exception {
        TranCatBalance tcb = makeTcb(9999L, "SA", 1000, "1000.00");
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(tcb));
        when(accountRepository.findById(9999L)).thenReturn(Optional.empty());

        tasklet.execute(stepContribution, chunkContext);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void execute_zeroRateSkipsInterest() throws Exception {
        TranCatBalance tcb = makeTcb(1001L, "SA", 1000, "5000.00");
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(tcb));

        Account acct = makeAccount(1001L, "1000.00", "50.00", "25.00", "GRP1");
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(acct));

        DisclosureGroup dg = new DisclosureGroup();
        dg.setIntRate(BigDecimal.ZERO);
        when(disclosureGroupRepository.findById(new DisclosureGroupId("GRP1", "SA", 1000)))
                .thenReturn(Optional.of(dg));

        tasklet.execute(stepContribution, chunkContext);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());

        Account saved = captor.getValue();
        // No interest applied, but cycle fields still reset
        assertEquals(new BigDecimal("1000.00"), saved.getCurrBal());
        assertEquals(BigDecimal.ZERO, saved.getCurrCycCredit());
        assertEquals(BigDecimal.ZERO, saved.getCurrCycDebit());
    }

    private TranCatBalance makeTcb(Long acctId, String typeCd, int catCd, String balance) {
        TranCatBalance tcb = new TranCatBalance();
        tcb.setAcctId(acctId);
        tcb.setTypeCd(typeCd);
        tcb.setCatCd(catCd);
        tcb.setTranCatBal(new BigDecimal(balance));
        return tcb;
    }

    private Account makeAccount(Long acctId, String bal, String cycCredit, String cycDebit, String groupId) {
        Account a = new Account();
        a.setAcctId(acctId);
        a.setCurrBal(new BigDecimal(bal));
        a.setCurrCycCredit(new BigDecimal(cycCredit));
        a.setCurrCycDebit(new BigDecimal(cycDebit));
        a.setGroupId(groupId);
        a.setActiveStatus("Y");
        return a;
    }
}
