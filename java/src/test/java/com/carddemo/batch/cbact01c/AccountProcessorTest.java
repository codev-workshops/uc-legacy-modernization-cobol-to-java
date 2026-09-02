package com.carddemo.batch.cbact01c;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carddemo.batch.cbact01c.date.CobdatftDateFormatter;
import com.carddemo.batch.cbact01c.date.DateFormatter;
import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrArrayRec;
import com.carddemo.batch.cbact01c.model.OutAcctRec;
import com.carddemo.batch.cbact01c.model.Vb1Rec;
import com.carddemo.batch.cbact01c.model.Vb2Rec;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AccountProcessorTest {

    private static final DateFormatter DF = new CobdatftDateFormatter();
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private static AccountRecord acct(String id, String debit) {
        return new AccountRecord(id, "Y", new BigDecimal("194.00"), new BigDecimal("2020.00"),
                new BigDecimal("1020.00"), "2014-11-20", "2025-05-20", "2025-05-20", ZERO,
                new BigDecimal(debit), "A000000000", "          ", new byte[AccountRecord.FILLER_LENGTH]);
    }

    @Test
    void debitZeroGets2525() {
        OutAcctRec out = AccountProcessor.populateOut(acct("00000000001", "0.00"), ZERO, DF);
        assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
        assertEquals("00000000001", out.acctId());
        assertEquals(new BigDecimal("194.00"), out.currBal());
        assertEquals("20250520  ", out.reissueDate());
        assertEquals("          ", out.groupId());
    }

    @Test
    void nonZeroDebitCarriesPreviousValue() {
        OutAcctRec first = AccountProcessor.populateOut(acct("00000000001", "0.00"),
                AccountProcessor.INITIAL_CARRIED_DEBIT, DF);
        OutAcctRec second = AccountProcessor.populateOut(acct("00000000002", "10.00"), first.currCycDebit(), DF);
        assertEquals(new BigDecimal("2525.00"), second.currCycDebit());

        OutAcctRec firstNonZero = AccountProcessor.populateOut(acct("00000000001", "10.00"),
                AccountProcessor.INITIAL_CARRIED_DEBIT, DF);
        assertEquals(ZERO, firstNonZero.currCycDebit());
    }

    @Test
    void arrayContents() {
        ArrArrayRec arr = AccountProcessor.populateArr(acct("00000000001", "0.00"));
        assertEquals("00000000001", arr.acctId());
        assertEquals(new BigDecimal("194.00"), arr.bal(0).currBal());
        assertEquals(new BigDecimal("1005.00"), arr.bal(0).currCycDebit());
        assertEquals(new BigDecimal("194.00"), arr.bal(1).currBal());
        assertEquals(new BigDecimal("1525.00"), arr.bal(1).currCycDebit());
        assertEquals(new BigDecimal("-1025.00"), arr.bal(2).currBal());
        assertEquals(new BigDecimal("-2500.00"), arr.bal(2).currCycDebit());
        for (int i = 3; i < ArrArrayRec.OCCURS; i++) {
            assertEquals(ArrArrayRec.ArrAcctBal.initialized(), arr.bal(i));
        }
        assertEquals("    ", arr.filler());
    }

    @Test
    void vbRecords() {
        AccountRecord a = acct("00000000001", "0.00");
        Vb1Rec vb1 = AccountProcessor.vb1(a);
        assertEquals(new Vb1Rec("00000000001", "Y"), vb1);
        Vb2Rec vb2 = AccountProcessor.vb2(a);
        assertEquals("00000000001", vb2.acctId());
        assertEquals(new BigDecimal("194.00"), vb2.currBal());
        assertEquals(new BigDecimal("2020.00"), vb2.creditLimit());
        assertEquals("2025", vb2.reissueYyyy());
        assertEquals("20250520  ", AccountProcessor.populateOut(a, ZERO, DF).reissueDate());
    }
}
