package com.carddemo.transaction.batch;

import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.transaction.entity.DisclosureGroup;
import com.carddemo.transaction.entity.TranCatBalance;
import com.carddemo.transaction.repository.DisclosureGroupRepository;
import com.carddemo.transaction.repository.TranCatBalanceRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestCalculationProcessorTest {

    @Mock
    private TranCatBalanceRepository tranCatBalanceRepository;

    @Mock
    private DisclosureGroupRepository disclosureGroupRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    private InterestCalculationJob interestJob;
    private ItemProcessor<TranCatBalance, TranCatBalance> processor;

    @BeforeEach
    void setUp() {
        interestJob = new InterestCalculationJob(
                tranCatBalanceRepository, disclosureGroupRepository,
                accountServiceClient, entityManagerFactory);
        processor = interestJob.new InterestProcessor();
    }

    @Test
    void testInterestComputationWithKnownValues() throws Exception {
        TranCatBalance tcb = TranCatBalance.builder()
                .trancatAcctId(1000L)
                .trancatTypeCd("01")
                .trancatCd(1)
                .tranCatBal(new BigDecimal("5000.00"))
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(1000L)
                .acctGroupId("GRP001")
                .acctCurrBal(new BigDecimal("5000.00"))
                .build();

        DisclosureGroup disclosure = DisclosureGroup.builder()
                .disAcctGroupId("GRP001")
                .disTranTypeCd("01")
                .disTranCatCd(1)
                .disIntRate(new BigDecimal("18.99"))
                .build();

        when(accountServiceClient.getAccount(1000L)).thenReturn(account);
        when(disclosureGroupRepository.findByDisAcctGroupIdAndDisTranTypeCdAndDisTranCatCd(
                "GRP001", "01", 1)).thenReturn(Optional.of(disclosure));
        when(accountServiceClient.updateAccount(eq(1000L), any())).thenReturn(account);

        TranCatBalance result = processor.process(tcb);

        assertNotNull(result);
        BigDecimal expectedInterest = new BigDecimal("5000.00")
                .multiply(new BigDecimal("18.99"))
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal expectedBalance = new BigDecimal("5000.00").add(expectedInterest);
        assertEquals(expectedBalance, result.getTranCatBal());
    }

    @Test
    void testNoDisclosureGroup() throws Exception {
        TranCatBalance tcb = TranCatBalance.builder()
                .trancatAcctId(2000L)
                .trancatTypeCd("01")
                .trancatCd(1)
                .tranCatBal(new BigDecimal("3000.00"))
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(2000L)
                .acctGroupId("GRP999")
                .build();

        when(accountServiceClient.getAccount(2000L)).thenReturn(account);
        when(disclosureGroupRepository.findByDisAcctGroupIdAndDisTranTypeCdAndDisTranCatCd(
                "GRP999", "01", 1)).thenReturn(Optional.empty());

        TranCatBalance result = processor.process(tcb);
        assertNull(result);
    }

    @Test
    void testNoAccountGroup() throws Exception {
        TranCatBalance tcb = TranCatBalance.builder()
                .trancatAcctId(3000L)
                .trancatTypeCd("01")
                .trancatCd(1)
                .tranCatBal(new BigDecimal("1000.00"))
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(3000L)
                .acctGroupId(null)
                .build();

        when(accountServiceClient.getAccount(3000L)).thenReturn(account);

        TranCatBalance result = processor.process(tcb);
        assertNull(result);
    }

    @Test
    void testZeroBalanceNoInterest() {
        BigDecimal result = InterestCalculationJob.computeMonthlyInterest(
                BigDecimal.ZERO, new BigDecimal("18.99"));
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testZeroRateNoInterest() {
        BigDecimal result = InterestCalculationJob.computeMonthlyInterest(
                new BigDecimal("5000.00"), BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testNegativeBalanceNoInterest() {
        BigDecimal result = InterestCalculationJob.computeMonthlyInterest(
                new BigDecimal("-100.00"), new BigDecimal("18.99"));
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testComputeMonthlyInterestKnownValue() {
        BigDecimal result = InterestCalculationJob.computeMonthlyInterest(
                new BigDecimal("12000.00"), new BigDecimal("12.00"));
        assertEquals(new BigDecimal("120.00"), result);
    }

    @Test
    void testFeignException() throws Exception {
        TranCatBalance tcb = TranCatBalance.builder()
                .trancatAcctId(9000L)
                .trancatTypeCd("01")
                .trancatCd(1)
                .tranCatBal(new BigDecimal("1000.00"))
                .build();

        when(accountServiceClient.getAccount(9000L))
                .thenThrow(new RuntimeException("Service unavailable"));

        TranCatBalance result = processor.process(tcb);
        assertNull(result);
    }
}
