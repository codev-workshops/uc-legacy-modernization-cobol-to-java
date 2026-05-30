package com.carddemo.transaction.batch;

import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.dto.CardXrefDto;
import com.carddemo.transaction.entity.DailyReject;
import com.carddemo.transaction.entity.DailyTransaction;
import com.carddemo.transaction.repository.DailyRejectRepository;
import com.carddemo.transaction.repository.DailyTransactionRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionValidationProcessorTest {

    @Mock
    private DailyRejectRepository dailyRejectRepository;

    @Mock
    private DailyTransactionRepository dailyTransactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    private TransactionValidationJob validationJob;
    private ItemProcessor<DailyTransaction, DailyTransaction> processor;

    @BeforeEach
    void setUp() {
        validationJob = new TransactionValidationJob(
                dailyRejectRepository, dailyTransactionRepository,
                accountServiceClient, entityManagerFactory);
        processor = validationJob.new ValidationProcessor();
    }

    @Test
    void testValidCard() throws Exception {
        DailyTransaction dt = createDailyTransaction("DT001", "4111111111111111");

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1000L)
                .xrefCustId(1L)
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(1000L)
                .acctActiveStatus("Y")
                .acctExpirationDate("2030-12-31")
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1000L)).thenReturn(account);

        DailyTransaction result = processor.process(dt);

        assertNotNull(result);
        assertEquals("DT001", result.getDalytranId());
        assertNotNull(result.getDalytranProcTs());
        verify(dailyRejectRepository, never()).save(any());
    }

    @Test
    void testExpiredCard() throws Exception {
        DailyTransaction dt = createDailyTransaction("DT002", "4222222222222222");

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4222222222222222")
                .xrefAcctId(2000L)
                .xrefCustId(2L)
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(2000L)
                .acctActiveStatus("Y")
                .acctExpirationDate("2020-01-01")
                .build();

        when(accountServiceClient.getCardXref("4222222222222222")).thenReturn(xref);
        when(accountServiceClient.getAccount(2000L)).thenReturn(account);

        DailyTransaction result = processor.process(dt);

        assertNull(result);
        ArgumentCaptor<DailyReject> captor = ArgumentCaptor.forClass(DailyReject.class);
        verify(dailyRejectRepository).save(captor.capture());
        assertTrue(captor.getValue().getRejectReason().contains("expired"));
    }

    @Test
    void testInactiveAccount() throws Exception {
        DailyTransaction dt = createDailyTransaction("DT003", "4333333333333333");

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4333333333333333")
                .xrefAcctId(3000L)
                .xrefCustId(3L)
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(3000L)
                .acctActiveStatus("N")
                .acctExpirationDate("2030-12-31")
                .build();

        when(accountServiceClient.getCardXref("4333333333333333")).thenReturn(xref);
        when(accountServiceClient.getAccount(3000L)).thenReturn(account);

        DailyTransaction result = processor.process(dt);

        assertNull(result);
        ArgumentCaptor<DailyReject> captor = ArgumentCaptor.forClass(DailyReject.class);
        verify(dailyRejectRepository).save(captor.capture());
        assertTrue(captor.getValue().getRejectReason().contains("not active"));
    }

    @Test
    void testMissingXref() throws Exception {
        DailyTransaction dt = createDailyTransaction("DT004", "4444444444444444");

        when(accountServiceClient.getCardXref("4444444444444444")).thenReturn(null);

        DailyTransaction result = processor.process(dt);

        assertNull(result);
        verify(dailyRejectRepository).save(any(DailyReject.class));
    }

    @Test
    void testFeignClientException() throws Exception {
        DailyTransaction dt = createDailyTransaction("DT005", "4555555555555555");

        when(accountServiceClient.getCardXref("4555555555555555"))
                .thenThrow(new RuntimeException("Service unavailable"));

        DailyTransaction result = processor.process(dt);

        assertNull(result);
        ArgumentCaptor<DailyReject> captor = ArgumentCaptor.forClass(DailyReject.class);
        verify(dailyRejectRepository).save(captor.capture());
        assertTrue(captor.getValue().getRejectReason().contains("Validation error"));
    }

    @Test
    void testIsCardExpired() {
        assertFalse(TransactionValidationJob.isCardExpired(null));
        assertFalse(TransactionValidationJob.isCardExpired(""));
        assertFalse(TransactionValidationJob.isCardExpired("2030-12-31"));
        assertTrue(TransactionValidationJob.isCardExpired("2020-01-01"));
        assertFalse(TransactionValidationJob.isCardExpired("invalid-date"));
    }

    private DailyTransaction createDailyTransaction(String id, String cardNum) {
        return DailyTransaction.builder()
                .dalytranId(id)
                .dalytranTypeCd("01")
                .dalytranCatCd(1)
                .dalytranSource("ONLINE")
                .dalytranDesc("Test transaction")
                .dalytranAmt(new BigDecimal("100.00"))
                .dalytranCardNum(cardNum)
                .dalytranOrigTs("2024-01-15-10.30.00.000000")
                .build();
    }
}
