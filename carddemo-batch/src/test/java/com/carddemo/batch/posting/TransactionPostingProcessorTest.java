package com.carddemo.batch.posting;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.DailyTransaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionPostingProcessorTest {

    private CardXrefRepository cardXrefRepository;
    private AccountRepository accountRepository;
    private TransactionPostingProcessor processor;

    @BeforeEach
    void setUp() {
        cardXrefRepository = mock(CardXrefRepository.class);
        accountRepository = mock(AccountRepository.class);
        processor = new TransactionPostingProcessor(cardXrefRepository, accountRepository);
    }

    @Test
    void accept_validTransaction() throws Exception {
        DailyTransaction dt = dailyTran("4111111111111111", "50.00", "2024-06-01 10:00:00.000");
        wireXrefAndAccount("4111111111111111", 11111111111L,
                "1000.00", "10000.00", "2025-12-31", "0.00", "0.00");

        PostingResult result = processor.process(dt);

        assertTrue(result.isAccepted());
        assertSame(dt, result.getSource());
    }

    @Test
    void reject_100_invalidCardNumber() throws Exception {
        DailyTransaction dt = dailyTran("9999999999999999", "50.00", "2024-06-01 10:00:00.000");
        when(cardXrefRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        PostingResult result = processor.process(dt);

        assertFalse(result.isAccepted());
        assertEquals(100, result.getRejected().getReasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", result.getRejected().getReasonDesc());
    }

    @Test
    void reject_101_accountNotFound() throws Exception {
        DailyTransaction dt = dailyTran("4111111111111111", "50.00", "2024-06-01 10:00:00.000");
        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setAcctId(99999999999L);
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(99999999999L)).thenReturn(Optional.empty());

        PostingResult result = processor.process(dt);

        assertFalse(result.isAccepted());
        assertEquals(101, result.getRejected().getReasonCode());
    }

    @Test
    void reject_102_overlimit() throws Exception {
        // cycCredit=9500, cycDebit=0, creditLimit=10000, tranAmt=600
        // tempBal = 9500 - 0 + 600 = 10100 > 10000
        DailyTransaction dt = dailyTran("4111111111111111", "600.00", "2024-06-01 10:00:00.000");
        wireXrefAndAccount("4111111111111111", 11111111111L,
                "5000.00", "10000.00", "2025-12-31", "9500.00", "0.00");

        PostingResult result = processor.process(dt);

        assertFalse(result.isAccepted());
        assertEquals(102, result.getRejected().getReasonCode());
        assertEquals("OVERLIMIT TRANSACTION", result.getRejected().getReasonDesc());
    }

    @Test
    void accept_exactlyAtCreditLimit() throws Exception {
        // cycCredit=9500, cycDebit=0, creditLimit=10000, tranAmt=500
        // tempBal = 9500 - 0 + 500 = 10000 == 10000 → accepted
        DailyTransaction dt = dailyTran("4111111111111111", "500.00", "2024-06-01 10:00:00.000");
        wireXrefAndAccount("4111111111111111", 11111111111L,
                "5000.00", "10000.00", "2025-12-31", "9500.00", "0.00");

        PostingResult result = processor.process(dt);

        assertTrue(result.isAccepted());
    }

    @Test
    void reject_103_expired() throws Exception {
        // Account expires 2023-12-31, tran date 2024-06-01
        DailyTransaction dt = dailyTran("4111111111111111", "50.00", "2024-06-01 10:00:00.000");
        wireXrefAndAccount("4111111111111111", 11111111111L,
                "1000.00", "10000.00", "2023-12-31", "0.00", "0.00");

        PostingResult result = processor.process(dt);

        assertFalse(result.isAccepted());
        assertEquals(103, result.getRejected().getReasonCode());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", result.getRejected().getReasonDesc());
    }

    @Test
    void accept_expirationSameDate() throws Exception {
        // Account expires 2024-06-01, tran date 2024-06-01 → accepted
        DailyTransaction dt = dailyTran("4111111111111111", "50.00", "2024-06-01 10:00:00.000");
        wireXrefAndAccount("4111111111111111", 11111111111L,
                "1000.00", "10000.00", "2024-06-01", "0.00", "0.00");

        PostingResult result = processor.process(dt);

        assertTrue(result.isAccepted());
    }

    @Test
    void reject_overlimitCheckedBeforeExpiration() throws Exception {
        // Both overlimit AND expired — COBOL checks credit limit first
        DailyTransaction dt = dailyTran("4111111111111111", "600.00", "2024-06-01 10:00:00.000");
        wireXrefAndAccount("4111111111111111", 11111111111L,
                "5000.00", "10000.00", "2023-12-31", "9500.00", "0.00");

        PostingResult result = processor.process(dt);

        assertFalse(result.isAccepted());
        assertEquals(102, result.getRejected().getReasonCode());
    }

    @Test
    void accept_negativeAmount() throws Exception {
        // Debit (negative amount) should still pass validation
        DailyTransaction dt = dailyTran("4111111111111111", "-100.00", "2024-06-01 10:00:00.000");
        wireXrefAndAccount("4111111111111111", 11111111111L,
                "5000.00", "10000.00", "2025-12-31", "500.00", "-200.00");

        PostingResult result = processor.process(dt);

        assertTrue(result.isAccepted());
    }

    @Test
    void rejectedTransaction_toString() {
        DailyTransaction dt = dailyTran("4111111111111111", "50.00", "2024-01-01 00:00:00.000");
        dt.setTranId("TRN0000000000001");
        RejectedTransaction rj = new RejectedTransaction(dt, 102, "OVERLIMIT TRANSACTION");
        String str = rj.toString();
        assertTrue(str.contains("102"));
        assertTrue(str.contains("TRN0000000000001"));
    }

    @Test
    void postingResult_factoryMethods() {
        DailyTransaction dt = dailyTran("4111111111111111", "50.00", "2024-01-01 00:00:00.000");

        PostingResult accepted = PostingResult.accepted(dt);
        assertTrue(accepted.isAccepted());
        assertSame(dt, accepted.getSource());
        assertNull(accepted.getRejected());

        PostingResult rejected = PostingResult.rejected(dt, 100, "TEST");
        assertFalse(rejected.isAccepted());
        assertSame(dt, rejected.getSource());
        assertNotNull(rejected.getRejected());
        assertEquals(100, rejected.getRejected().getReasonCode());
    }

    // --- helpers ---

    private DailyTransaction dailyTran(String cardNum, String amt, String origTs) {
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId("TRN0000000000001");
        dt.setTypeCd("SA");
        dt.setCatCd(5001);
        dt.setSource("POS");
        dt.setDesc("Test");
        dt.setAmt(new BigDecimal(amt));
        dt.setMerchantId(900001L);
        dt.setMerchantName("Merchant");
        dt.setMerchantCity("City");
        dt.setMerchantZip("10001");
        dt.setCardNum(cardNum);
        dt.setOrigTs(origTs);
        return dt;
    }

    private void wireXrefAndAccount(String cardNum, Long acctId,
                                     String currBal, String creditLimit,
                                     String expirationDate,
                                     String cycCredit, String cycDebit) {
        CardXref xref = new CardXref();
        xref.setXrefCardNum(cardNum);
        xref.setAcctId(acctId);
        xref.setCustId(1000001L);
        when(cardXrefRepository.findById(cardNum)).thenReturn(Optional.of(xref));

        Account acct = new Account();
        acct.setAcctId(acctId);
        acct.setActiveStatus("Y");
        acct.setCurrBal(new BigDecimal(currBal));
        acct.setCreditLimit(new BigDecimal(creditLimit));
        acct.setExpirationDate(expirationDate);
        acct.setCurrCycCredit(new BigDecimal(cycCredit));
        acct.setCurrCycDebit(new BigDecimal(cycDebit));
        when(accountRepository.findById(acctId)).thenReturn(Optional.of(acct));
    }
}
