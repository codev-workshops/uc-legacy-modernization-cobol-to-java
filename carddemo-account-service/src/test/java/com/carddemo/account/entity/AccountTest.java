package com.carddemo.account.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Account account = Account.builder()
                .acctId(12345678901L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("1000.50"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("2000.00"))
                .acctOpenDate("2020-01-15")
                .acctExpirationDate("2025-01-15")
                .acctReissueDate("2023-01-15")
                .acctCurrCycCredit(new BigDecimal("200.00"))
                .acctCurrCycDebit(new BigDecimal("150.00"))
                .acctAddrZip("10001")
                .acctGroupId("GRP001")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(12345678901L, account.getAcctId());
        assertEquals("Y", account.getAcctActiveStatus());
        assertEquals(new BigDecimal("1000.50"), account.getAcctCurrBal());
        assertEquals(new BigDecimal("5000.00"), account.getAcctCreditLimit());
        assertEquals(new BigDecimal("2000.00"), account.getAcctCashCreditLimit());
        assertEquals("2020-01-15", account.getAcctOpenDate());
        assertEquals("2025-01-15", account.getAcctExpirationDate());
        assertEquals("2023-01-15", account.getAcctReissueDate());
        assertEquals(new BigDecimal("200.00"), account.getAcctCurrCycCredit());
        assertEquals(new BigDecimal("150.00"), account.getAcctCurrCycDebit());
        assertEquals("10001", account.getAcctAddrZip());
        assertEquals("GRP001", account.getAcctGroupId());
        assertEquals(now, account.getCreatedAt());
        assertEquals(now, account.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Account account = new Account();
        assertNull(account.getAcctId());
        assertNull(account.getAcctActiveStatus());
    }

    @Test
    void testSetters() {
        Account account = new Account();
        account.setAcctId(1L);
        account.setAcctActiveStatus("N");
        assertEquals(1L, account.getAcctId());
        assertEquals("N", account.getAcctActiveStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        Account a1 = Account.builder().acctId(1L).acctActiveStatus("Y").build();
        Account a2 = Account.builder().acctId(1L).acctActiveStatus("Y").build();
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void testToString() {
        Account account = Account.builder().acctId(1L).build();
        assertNotNull(account.toString());
        assertTrue(account.toString().contains("1"));
    }
}
