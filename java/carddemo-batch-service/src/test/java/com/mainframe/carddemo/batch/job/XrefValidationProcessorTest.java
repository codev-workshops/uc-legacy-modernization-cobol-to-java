package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.DailyTransaction;
import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XrefValidationProcessorTest {

    @Mock
    private AccountServiceClient accountServiceClient;

    private XrefValidationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new XrefValidationProcessor(accountServiceClient);
    }

    @Test
    void process_validCard_setsAccountId() throws Exception {
        DailyTransaction txn = createTransaction("4111111111111111");
        CardXrefDto xref = new CardXrefDto();
        xref.setCardNum("4111111111111111");
        xref.setAccountId(100L);
        when(accountServiceClient.getXrefByCardNum("4111111111111111")).thenReturn(xref);

        PostingResult result = processor.process(txn);

        assertFalse(result.isRejected());
        assertEquals(100L, result.getAccountId());
    }

    @Test
    void process_cardNotFound_rejects100() throws Exception {
        DailyTransaction txn = createTransaction("0000000000000000");
        when(accountServiceClient.getXrefByCardNum("0000000000000000")).thenReturn(null);

        PostingResult result = processor.process(txn);

        assertTrue(result.isRejected());
        assertEquals(100, result.getRejectReason());
    }

    @Test
    void process_feignException_rejects100() throws Exception {
        DailyTransaction txn = createTransaction("1234567890123456");
        when(accountServiceClient.getXrefByCardNum("1234567890123456"))
                .thenThrow(new RuntimeException("Service down"));

        PostingResult result = processor.process(txn);

        assertTrue(result.isRejected());
        assertEquals(100, result.getRejectReason());
    }

    private DailyTransaction createTransaction(String cardNum) {
        DailyTransaction txn = new DailyTransaction();
        txn.setTranId("T001");
        txn.setTranCardNum(cardNum);
        return txn;
    }
}
