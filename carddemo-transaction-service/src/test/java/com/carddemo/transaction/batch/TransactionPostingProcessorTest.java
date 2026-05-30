package com.carddemo.transaction.batch;

import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.dto.CardXrefDto;
import com.carddemo.transaction.entity.DailyReject;
import com.carddemo.transaction.entity.DailyTransaction;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.repository.DailyRejectRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionPostingProcessorTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private DailyRejectRepository dailyRejectRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    private TransactionPostingJob postingJob;
    private ItemProcessor<DailyTransaction, Transaction> processor;

    @BeforeEach
    void setUp() {
        postingJob = new TransactionPostingJob(
                transactionRepository, dailyRejectRepository,
                accountServiceClient, entityManagerFactory);
        processor = postingJob.new PostingProcessor();
    }

    @Test
    void testWithinCreditLimit() throws Exception {
        DailyTransaction dt = createDailyTransaction("DT001", "01", new BigDecimal("100.00"));

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1000L)
                .xrefCustId(1L)
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(1000L)
                .acctActiveStatus("Y")
                .acctExpirationDate("2030-12-31")
                .acctCreditLimit(new BigDecimal("10000.00"))
                .acctCurrCycCredit(BigDecimal.ZERO)
                .acctCurrCycDebit(new BigDecimal("500.00"))
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1000L)).thenReturn(account);
        when(accountServiceClient.updateAccount(eq(1000L), any())).thenReturn(account);

        Transaction result = processor.process(dt);

        assertNotNull(result);
        assertEquals("DT001", result.getTranId());
        assertEquals("01", result.getTranTypeCd());
        assertEquals(new BigDecimal("100.00"), result.getTranAmt());
        verify(accountServiceClient).updateAccount(eq(1000L), any());
    }

    @Test
    void testOverCreditLimit() throws Exception {
        DailyTransaction dt = createDailyTransaction("DT002", "01", new BigDecimal("20000.00"));

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1000L)
                .xrefCustId(1L)
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(1000L)
                .acctActiveStatus("Y")
                .acctExpirationDate("2030-12-31")
                .acctCreditLimit(new BigDecimal("10000.00"))
                .acctCurrCycCredit(BigDecimal.ZERO)
                .acctCurrCycDebit(new BigDecimal("500.00"))
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1000L)).thenReturn(account);

        Transaction result = processor.process(dt);

        assertNull(result);
        verify(dailyRejectRepository).save(any(DailyReject.class));
    }

    @Test
    void testExpiredCard() throws Exception {
        DailyTransaction dt = createDailyTransaction("DT003", "01", new BigDecimal("100.00"));

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1000L)
                .xrefCustId(1L)
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(1000L)
                .acctActiveStatus("Y")
                .acctExpirationDate("2020-01-01")
                .acctCreditLimit(new BigDecimal("10000.00"))
                .acctCurrCycCredit(BigDecimal.ZERO)
                .acctCurrCycDebit(BigDecimal.ZERO)
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1000L)).thenReturn(account);

        Transaction result = processor.process(dt);

        assertNull(result);
        ArgumentCaptor<DailyReject> captor = ArgumentCaptor.forClass(DailyReject.class);
        verify(dailyRejectRepository).save(captor.capture());
        assertTrue(captor.getValue().getRejectReason().contains("expired"));
    }

    @Test
    void testPaymentTransactionUpdatesCredit() throws Exception {
        DailyTransaction dt = createDailyTransaction("DT004", "02", new BigDecimal("500.00"));

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1000L)
                .xrefCustId(1L)
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(1000L)
                .acctActiveStatus("Y")
                .acctExpirationDate("2030-12-31")
                .acctCreditLimit(new BigDecimal("10000.00"))
                .acctCurrCycCredit(BigDecimal.ZERO)
                .acctCurrCycDebit(BigDecimal.ZERO)
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1000L)).thenReturn(account);
        when(accountServiceClient.updateAccount(eq(1000L), any())).thenReturn(account);

        Transaction result = processor.process(dt);

        assertNotNull(result);
        ArgumentCaptor<AccountDto> captor = ArgumentCaptor.forClass(AccountDto.class);
        verify(accountServiceClient).updateAccount(eq(1000L), captor.capture());
        assertEquals(new BigDecimal("500.00"), captor.getValue().getAcctCurrCycCredit());
    }

    @Test
    void testIsWithinCreditLimitStatic() {
        AccountDto account = AccountDto.builder()
                .acctCreditLimit(new BigDecimal("10000.00"))
                .acctCurrCycCredit(BigDecimal.ZERO)
                .acctCurrCycDebit(new BigDecimal("2000.00"))
                .build();

        assertTrue(TransactionPostingJob.isWithinCreditLimit(account, new BigDecimal("5000.00")));
        assertFalse(TransactionPostingJob.isWithinCreditLimit(account, new BigDecimal("15000.00")));

        AccountDto nullBalanceAccount = AccountDto.builder()
                .acctCreditLimit(new BigDecimal("5000.00"))
                .build();
        assertTrue(TransactionPostingJob.isWithinCreditLimit(nullBalanceAccount, new BigDecimal("100.00")));
    }

    private DailyTransaction createDailyTransaction(String id, String typeCd, BigDecimal amount) {
        return DailyTransaction.builder()
                .dalytranId(id)
                .dalytranTypeCd(typeCd)
                .dalytranCatCd(1)
                .dalytranSource("ONLINE")
                .dalytranDesc("Test transaction")
                .dalytranAmt(amount)
                .dalytranCardNum("4111111111111111")
                .dalytranOrigTs("2024-01-15-10.30.00.000000")
                .dalytranProcTs("2024-01-15-10.30.01.000000")
                .build();
    }
}
