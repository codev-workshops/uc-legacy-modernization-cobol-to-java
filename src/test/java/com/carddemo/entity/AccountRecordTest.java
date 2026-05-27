package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AccountRecordTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CVACT01Y.cpy has 12 non-FILLER fields
        assertEquals(12, AccountRecord.class.getDeclaredFields().length);
    }

    @Test
    void financialFieldsAreBigDecimal() throws NoSuchFieldException {
        assertEquals(BigDecimal.class, AccountRecord.class.getDeclaredField("acctCurrBal").getType());
        assertEquals(BigDecimal.class, AccountRecord.class.getDeclaredField("acctCreditLimit").getType());
        assertEquals(BigDecimal.class, AccountRecord.class.getDeclaredField("acctCashCreditLimit").getType());
        assertEquals(BigDecimal.class, AccountRecord.class.getDeclaredField("acctCurrCycCredit").getType());
        assertEquals(BigDecimal.class, AccountRecord.class.getDeclaredField("acctCurrCycDebit").getType());
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new AccountRecord();
        record.setAcctId(12345678901L);
        record.setAcctActiveStatus("Y");
        record.setAcctCurrBal(new BigDecimal("1000.50"));
        record.setAcctCreditLimit(new BigDecimal("5000.00"));
        record.setAcctCashCreditLimit(new BigDecimal("2000.00"));
        record.setAcctOpenDate(LocalDate.of(2020, 1, 15));
        record.setAcctExpirationDate(LocalDate.of(2025, 12, 31));
        record.setAcctReissueDate(LocalDate.of(2023, 6, 1));
        record.setAcctCurrCycCredit(new BigDecimal("500.00"));
        record.setAcctCurrCycDebit(new BigDecimal("200.00"));
        record.setAcctAddrZip("10001");
        record.setAcctGroupId("GRP001");

        assertEquals(12345678901L, record.getAcctId());
        assertEquals("Y", record.getAcctActiveStatus());
        assertEquals(new BigDecimal("1000.50"), record.getAcctCurrBal());
        assertEquals(LocalDate.of(2020, 1, 15), record.getAcctOpenDate());
    }
}
