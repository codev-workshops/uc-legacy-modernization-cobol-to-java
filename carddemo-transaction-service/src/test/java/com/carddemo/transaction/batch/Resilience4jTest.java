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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Resilience4jTest {

    @Mock
    private DailyRejectRepository dailyRejectRepository;

    @Mock
    private DailyTransactionRepository dailyTransactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    private TransactionValidationJob validationJob;

    @BeforeEach
    void setUp() {
        validationJob = new TransactionValidationJob(
                dailyRejectRepository, dailyTransactionRepository,
                accountServiceClient, entityManagerFactory);
    }

    @Test
    void testCircuitBreakerOnFeignFailure() throws Exception {
        ItemProcessor<DailyTransaction, DailyTransaction> processor =
                validationJob.new ValidationProcessor();

        DailyTransaction dt = DailyTransaction.builder()
                .dalytranId("R4J-001")
                .dalytranTypeCd("01")
                .dalytranCatCd(1)
                .dalytranAmt(new BigDecimal("100.00"))
                .dalytranCardNum("4111111111111111")
                .build();

        when(accountServiceClient.getCardXref(any()))
                .thenThrow(new RuntimeException("Connection refused"));

        DailyTransaction result = processor.process(dt);

        assertNull(result);
        verify(dailyRejectRepository).save(any(DailyReject.class));
    }

    @Test
    void testMultipleFailuresAreCaptured() throws Exception {
        ItemProcessor<DailyTransaction, DailyTransaction> processor =
                validationJob.new ValidationProcessor();

        when(accountServiceClient.getCardXref(any()))
                .thenThrow(new RuntimeException("Timeout"));

        for (int i = 0; i < 5; i++) {
            DailyTransaction dt = DailyTransaction.builder()
                    .dalytranId("R4J-" + i)
                    .dalytranTypeCd("01")
                    .dalytranCatCd(1)
                    .dalytranAmt(new BigDecimal("100.00"))
                    .dalytranCardNum("4111111111111111")
                    .build();

            DailyTransaction result = processor.process(dt);
            assertNull(result);
        }

        verify(dailyRejectRepository, times(5)).save(any(DailyReject.class));
    }

    @Test
    void testFeignSuccessAfterFailure() throws Exception {
        ItemProcessor<DailyTransaction, DailyTransaction> processor =
                validationJob.new ValidationProcessor();

        when(accountServiceClient.getCardXref("4111111111111111"))
                .thenThrow(new RuntimeException("Timeout"));

        DailyTransaction dt1 = DailyTransaction.builder()
                .dalytranId("R4J-F1")
                .dalytranTypeCd("01")
                .dalytranCatCd(1)
                .dalytranAmt(new BigDecimal("100.00"))
                .dalytranCardNum("4111111111111111")
                .build();

        assertNull(processor.process(dt1));

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4222222222222222")
                .xrefAcctId(2000L)
                .xrefCustId(2L)
                .build();
        AccountDto account = AccountDto.builder()
                .acctId(2000L)
                .acctActiveStatus("Y")
                .acctExpirationDate("2030-12-31")
                .build();

        when(accountServiceClient.getCardXref("4222222222222222")).thenReturn(xref);
        when(accountServiceClient.getAccount(2000L)).thenReturn(account);

        DailyTransaction dt2 = DailyTransaction.builder()
                .dalytranId("R4J-S1")
                .dalytranTypeCd("01")
                .dalytranCatCd(1)
                .dalytranAmt(new BigDecimal("100.00"))
                .dalytranCardNum("4222222222222222")
                .build();

        DailyTransaction result = processor.process(dt2);
        assertNotNull(result);
    }
}
