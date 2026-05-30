package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.DailyTransaction;
import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.AccountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountValidationProcessorTest {

    @Mock
    private AccountServiceClient accountServiceClient;

    private AccountValidationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new AccountValidationProcessor(accountServiceClient);
    }

    @Test
    void process_validAccount_setsAccountDetails() throws Exception {
        PostingResult input = createPostingResult(100L, false);
        AccountDto account = new AccountDto();
        account.setAccountId(100L);
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("50.00"));
        account.setExpirationDate(LocalDate.of(2027, 12, 31));
        when(accountServiceClient.getInternalAccountById(100L)).thenReturn(account);

        PostingResult result = processor.process(input);

        assertFalse(result.isRejected());
        assertEquals(new BigDecimal("5000.00"), result.getCreditLimit());
        assertEquals(new BigDecimal("200.00"), result.getCurrentCycleCredit());
        assertNotNull(result.getExpirationDate());
    }

    @Test
    void process_accountNotFound_rejects101() throws Exception {
        PostingResult input = createPostingResult(999L, false);
        when(accountServiceClient.getInternalAccountById(999L)).thenReturn(null);

        PostingResult result = processor.process(input);

        assertTrue(result.isRejected());
        assertEquals(101, result.getRejectReason());
    }

    @Test
    void process_alreadyRejected_passesThrough() throws Exception {
        PostingResult input = createPostingResult(100L, true);

        PostingResult result = processor.process(input);

        assertTrue(result.isRejected());
        assertEquals(100, result.getRejectReason());
    }

    @Test
    void process_feignException_rejects101() throws Exception {
        PostingResult input = createPostingResult(100L, false);
        when(accountServiceClient.getInternalAccountById(100L))
                .thenThrow(new RuntimeException("Service down"));

        PostingResult result = processor.process(input);

        assertTrue(result.isRejected());
        assertEquals(101, result.getRejectReason());
    }

    private PostingResult createPostingResult(Long acctId, boolean rejected) {
        DailyTransaction txn = new DailyTransaction();
        txn.setTranId("T001");
        PostingResult result = new PostingResult(txn);
        result.setAccountId(acctId);
        if (rejected) {
            result.reject(100, "Already rejected");
        }
        return result;
    }
}
