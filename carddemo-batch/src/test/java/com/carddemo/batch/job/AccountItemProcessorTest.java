package com.carddemo.batch.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.carddemo.batch.model.AccountOutputBundle;
import com.carddemo.batch.model.ArryfileRecord;
import com.carddemo.batch.model.OutfileRecord;
import com.carddemo.batch.model.VbrcRec1;
import com.carddemo.batch.model.VbrcRec2;
import com.carddemo.common.entity.Account;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountItemProcessorTest {

    private AccountItemProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new AccountItemProcessor();
    }

    private Account createAccount(long id, BigDecimal currBal, BigDecimal cycDebit,
                                  String reissueDate) {
        Account acct = new Account();
        acct.setAcctId(id);
        acct.setActiveStatus("Y");
        acct.setCurrBal(currBal);
        acct.setCreditLimit(new BigDecimal("20000.00"));
        acct.setCashCreditLimit(new BigDecimal("10000.00"));
        acct.setOpenDate("2014-11-20");
        acct.setExpirationDate("2025-05-20");
        acct.setReissueDate(reissueDate);
        acct.setCurrCycCredit(new BigDecimal("500.00"));
        acct.setCurrCycDebit(cycDebit);
        acct.setGroupId("A000000000");
        return acct;
    }

    @Test
    void testZeroDebitSubstitution() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                BigDecimal.ZERO, "2025-05-20");
        OutfileRecord rec = processor.buildOutfileRecord(acct);
        assertEquals(new BigDecimal("2525.00"), rec.getCurrCycDebit());
    }

    @Test
    void testNullDebitSubstitution() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                null, "2025-05-20");
        OutfileRecord rec = processor.buildOutfileRecord(acct);
        assertEquals(new BigDecimal("2525.00"), rec.getCurrCycDebit());
    }

    @Test
    void testNonZeroDebitPassthrough() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                new BigDecimal("1500.00"), "2025-05-20");
        OutfileRecord rec = processor.buildOutfileRecord(acct);
        assertEquals(new BigDecimal("1500.00"), rec.getCurrCycDebit());
    }

    @Test
    void testDateTruncation() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                new BigDecimal("100.00"), "2025-05-20");
        OutfileRecord rec = processor.buildOutfileRecord(acct);
        // COBDATFT with type 2→2 produces "2025-05-" (first 8 of "2025-05-20          ")
        assertEquals(8, rec.getReissueDate().length());
        assertEquals("2025-05-", rec.getReissueDate());
    }

    @Test
    void testDateTruncationBlankInput() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                new BigDecimal("100.00"), "");
        OutfileRecord rec = processor.buildOutfileRecord(acct);
        assertEquals(8, rec.getReissueDate().length());
    }

    @Test
    void testDateTruncationNullInput() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                new BigDecimal("100.00"), null);
        OutfileRecord rec = processor.buildOutfileRecord(acct);
        assertEquals(8, rec.getReissueDate().length());
    }

    @Test
    void testArrayRecordHardcodedValues() {
        BigDecimal bal = new BigDecimal("1940.00");
        Account acct = createAccount(10000000001L, bal, BigDecimal.ZERO, "2025-05-20");
        ArryfileRecord rec = processor.buildArryfileRecord(acct);

        assertEquals(10000000001L, rec.getAcctId());

        // Slot 0 (COBOL 1): balance = currBal, cycDebit = 1005.00
        assertEquals(bal, rec.getBalance(0));
        assertEquals(new BigDecimal("1005.00"), rec.getCycDebit(0));

        // Slot 1 (COBOL 2): balance = currBal, cycDebit = 1525.00
        assertEquals(bal, rec.getBalance(1));
        assertEquals(new BigDecimal("1525.00"), rec.getCycDebit(1));

        // Slot 2 (COBOL 3): balance = -1025.00, cycDebit = -2500.00
        assertEquals(new BigDecimal("-1025.00"), rec.getBalance(2));
        assertEquals(new BigDecimal("-2500.00"), rec.getCycDebit(2));
    }

    @Test
    void testArrayRecordUnpopulatedSlots() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                BigDecimal.ZERO, "2025-05-20");
        ArryfileRecord rec = processor.buildArryfileRecord(acct);

        // Slots 3-4 (COBOL 4-5) remain at zero
        assertEquals(BigDecimal.ZERO, rec.getBalance(3));
        assertEquals(BigDecimal.ZERO, rec.getCycDebit(3));
        assertEquals(BigDecimal.ZERO, rec.getBalance(4));
        assertEquals(BigDecimal.ZERO, rec.getCycDebit(4));
    }

    @Test
    void testVbrcRec1() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                BigDecimal.ZERO, "2025-05-20");
        VbrcRec1 rec = processor.buildVbrcRec1(acct);

        assertEquals(10000000001L, rec.getAcctId());
        assertEquals("Y", rec.getActiveStatus());
    }

    @Test
    void testVbrcRec2() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                BigDecimal.ZERO, "2025-05-20");
        VbrcRec2 rec = processor.buildVbrcRec2(acct);

        assertEquals(10000000001L, rec.getAcctId());
        assertEquals(new BigDecimal("1940.00"), rec.getCurrBal());
        assertEquals(new BigDecimal("20000.00"), rec.getCreditLimit());
        assertEquals("2025", rec.getReissueYyyy());
    }

    @Test
    void testVbrcRec2NullReissueDate() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                BigDecimal.ZERO, null);
        VbrcRec2 rec = processor.buildVbrcRec2(acct);
        assertEquals("    ", rec.getReissueYyyy());
    }

    @Test
    void testVbrcRec2ShortReissueDate() {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                BigDecimal.ZERO, "20");
        VbrcRec2 rec = processor.buildVbrcRec2(acct);
        assertEquals("    ", rec.getReissueYyyy());
    }

    @Test
    void testProcessReturnsAllRecords() throws Exception {
        Account acct = createAccount(10000000001L, new BigDecimal("1940.00"),
                new BigDecimal("100.00"), "2025-05-20");
        AccountOutputBundle bundle = processor.process(acct);

        assertNotNull(bundle);
        assertNotNull(bundle.getOutfileRecord());
        assertNotNull(bundle.getArryfileRecord());
        assertNotNull(bundle.getVbrcRec1());
        assertNotNull(bundle.getVbrcRec2());
    }

    @Test
    void testOutfileFieldMapping() {
        Account acct = createAccount(10000000005L, new BigDecimal("-1025.00"),
                new BigDecimal("4025.00"), "2026-03-15");
        OutfileRecord rec = processor.buildOutfileRecord(acct);

        assertEquals(10000000005L, rec.getAcctId());
        assertEquals("Y", rec.getActiveStatus());
        assertEquals(new BigDecimal("-1025.00"), rec.getCurrBal());
        assertEquals(new BigDecimal("20000.00"), rec.getCreditLimit());
        assertEquals(new BigDecimal("10000.00"), rec.getCashCreditLimit());
        assertEquals("2014-11-20", rec.getOpenDate());
        assertEquals("2025-05-20", rec.getExpirationDate());
        assertEquals(new BigDecimal("500.00"), rec.getCurrCycCredit());
        assertEquals(new BigDecimal("4025.00"), rec.getCurrCycDebit());
        assertEquals("A000000000", rec.getGroupId());
    }
}
