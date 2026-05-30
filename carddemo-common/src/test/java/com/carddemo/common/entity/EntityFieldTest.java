package com.carddemo.common.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EntityFieldTest {

    @Test
    void accountAllFields() {
        Account a = new Account();
        a.setAcctId(12345L);
        a.setActiveStatus("Y");
        a.setCurrBal(new BigDecimal("100.00"));
        a.setCreditLimit(new BigDecimal("5000.00"));
        a.setCashCreditLimit(new BigDecimal("2000.00"));
        a.setOpenDate("2020-01-01");
        a.setExpirationDate("2025-12-31");
        a.setReissueDate("2024-06-15");
        a.setCurrCycCredit(new BigDecimal("300.00"));
        a.setCurrCycDebit(new BigDecimal("200.00"));
        a.setAddrZip("12345");
        a.setGroupId("GRP001");

        assertEquals(12345L, a.getAcctId());
        assertEquals("Y", a.getActiveStatus());
        assertEquals(new BigDecimal("100.00"), a.getCurrBal());
        assertEquals(new BigDecimal("5000.00"), a.getCreditLimit());
        assertEquals(new BigDecimal("2000.00"), a.getCashCreditLimit());
        assertEquals("2020-01-01", a.getOpenDate());
        assertEquals("2025-12-31", a.getExpirationDate());
        assertEquals("2024-06-15", a.getReissueDate());
        assertEquals(new BigDecimal("300.00"), a.getCurrCycCredit());
        assertEquals(new BigDecimal("200.00"), a.getCurrCycDebit());
        assertEquals("12345", a.getAddrZip());
        assertEquals("GRP001", a.getGroupId());
    }

    @Test
    void customerAllFields() {
        Customer c = new Customer();
        c.setCustId(999L);
        c.setFirstName("Alice");
        c.setMiddleName("B");
        c.setLastName("Smith");
        c.setAddrLine1("123 Main");
        c.setAddrLine2("Apt 1");
        c.setAddrLine3("Floor 2");
        c.setStateCode("CA");
        c.setCountryCode("USA");
        c.setZip("90210");
        c.setPhone1("555-0001");
        c.setPhone2("555-0002");
        c.setSsn(123456789L);
        c.setGovtIssuedId("DL123");
        c.setDob("1990-01-01");
        c.setEftAccountId("EFT001");
        c.setPriCardHolderInd("Y");
        c.setFicoCreditScore(750);

        assertEquals(999L, c.getCustId());
        assertEquals("Alice", c.getFirstName());
        assertEquals("B", c.getMiddleName());
        assertEquals("Smith", c.getLastName());
        assertEquals("123 Main", c.getAddrLine1());
        assertEquals("Apt 1", c.getAddrLine2());
        assertEquals("Floor 2", c.getAddrLine3());
        assertEquals("CA", c.getStateCode());
        assertEquals("USA", c.getCountryCode());
        assertEquals("90210", c.getZip());
        assertEquals("555-0001", c.getPhone1());
        assertEquals("555-0002", c.getPhone2());
        assertEquals(123456789L, c.getSsn());
        assertEquals("DL123", c.getGovtIssuedId());
        assertEquals("1990-01-01", c.getDob());
        assertEquals("EFT001", c.getEftAccountId());
        assertEquals("Y", c.getPriCardHolderInd());
        assertEquals(750, c.getFicoCreditScore());
    }

    @Test
    void transactionAllFields() {
        Transaction t = new Transaction();
        t.setTranId("TRAN0001");
        t.setTypeCd("01");
        t.setCatCd(1);
        t.setSource("POS TERM");
        t.setDesc("Test purchase");
        t.setAmt(new BigDecimal("99.99"));
        t.setMerchantId(12345L);
        t.setMerchantName("Test Store");
        t.setMerchantCity("New York");
        t.setMerchantZip("10001");
        t.setCardNum("4500000000000001");
        t.setOrigTs("2024-01-15 10:30:00");
        t.setProcTs("2024-01-15 10:30:01");

        assertEquals("TRAN0001", t.getTranId());
        assertEquals("01", t.getTypeCd());
        assertEquals(1, t.getCatCd());
        assertEquals("POS TERM", t.getSource());
        assertEquals("Test purchase", t.getDesc());
        assertEquals(new BigDecimal("99.99"), t.getAmt());
        assertEquals(12345L, t.getMerchantId());
        assertEquals("Test Store", t.getMerchantName());
        assertEquals("New York", t.getMerchantCity());
        assertEquals("10001", t.getMerchantZip());
        assertEquals("4500000000000001", t.getCardNum());
        assertEquals("2024-01-15 10:30:00", t.getOrigTs());
        assertEquals("2024-01-15 10:30:01", t.getProcTs());
    }

    @Test
    void dailyTransactionAllFields() {
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId("DT0001");
        dt.setTypeCd("02");
        dt.setCatCd(3);
        dt.setSource("ONLINE");
        dt.setDesc("Daily tran");
        dt.setAmt(new BigDecimal("150.00"));
        dt.setMerchantId(67890L);
        dt.setMerchantName("Daily Store");
        dt.setMerchantCity("Chicago");
        dt.setMerchantZip("60601");
        dt.setCardNum("4500000000000002");
        dt.setOrigTs("2024-02-01 09:00:00");
        dt.setProcTs("2024-02-01 09:00:01");

        assertEquals("DT0001", dt.getTranId());
        assertEquals("02", dt.getTypeCd());
        assertEquals(3, dt.getCatCd());
        assertEquals("ONLINE", dt.getSource());
        assertEquals("Daily tran", dt.getDesc());
        assertEquals(new BigDecimal("150.00"), dt.getAmt());
        assertEquals(67890L, dt.getMerchantId());
        assertEquals("Daily Store", dt.getMerchantName());
        assertEquals("Chicago", dt.getMerchantCity());
        assertEquals("60601", dt.getMerchantZip());
        assertEquals("4500000000000002", dt.getCardNum());
        assertEquals("2024-02-01 09:00:00", dt.getOrigTs());
        assertEquals("2024-02-01 09:00:01", dt.getProcTs());
    }

    @Test
    void cardAllFields() {
        Card c = new Card();
        c.setCardNum("4500111122223333");
        c.setAcctId(55555L);
        c.setCvvCd(456);
        c.setEmbossedName("TEST CARD");
        c.setExpirationDate("2026-12-31");
        c.setActiveStatus("Y");

        assertEquals("4500111122223333", c.getCardNum());
        assertEquals(55555L, c.getAcctId());
        assertEquals(456, c.getCvvCd());
        assertEquals("TEST CARD", c.getEmbossedName());
        assertEquals("2026-12-31", c.getExpirationDate());
        assertEquals("Y", c.getActiveStatus());
    }

    @Test
    void cardXrefAllFields() {
        CardXref x = new CardXref();
        x.setXrefCardNum("4500444455556666");
        x.setCustId(111L);
        x.setAcctId(222L);

        assertEquals("4500444455556666", x.getXrefCardNum());
        assertEquals(111L, x.getCustId());
        assertEquals(222L, x.getAcctId());
    }

    @Test
    void userAllFields() {
        User u = new User();
        u.setUsrId("USER01");
        u.setFname("First");
        u.setLname("Last");
        u.setPwd("secret");
        u.setUsrType("A");

        assertEquals("USER01", u.getUsrId());
        assertEquals("First", u.getFname());
        assertEquals("Last", u.getLname());
        assertEquals("secret", u.getPwd());
        assertEquals("A", u.getUsrType());
    }

    @Test
    void tranTypeAllFields() {
        TranType tt = new TranType();
        tt.setTranType("01");
        tt.setTranTypeDesc("Purchase");

        assertEquals("01", tt.getTranType());
        assertEquals("Purchase", tt.getTranTypeDesc());
    }

    @Test
    void tranCategoryAllFields() {
        TranCategory tc = new TranCategory();
        tc.setTypeCd("01");
        tc.setCatCd(1);
        tc.setTranCatTypeDesc("Regular");

        assertEquals("01", tc.getTypeCd());
        assertEquals(1, tc.getCatCd());
        assertEquals("Regular", tc.getTranCatTypeDesc());
    }

    @Test
    void tranCatBalanceAllFields() {
        TranCatBalance tcb = new TranCatBalance();
        tcb.setAcctId(100L);
        tcb.setTypeCd("01");
        tcb.setCatCd(1);
        tcb.setTranCatBal(new BigDecimal("500.00"));

        assertEquals(100L, tcb.getAcctId());
        assertEquals("01", tcb.getTypeCd());
        assertEquals(1, tcb.getCatCd());
        assertEquals(new BigDecimal("500.00"), tcb.getTranCatBal());
    }

    @Test
    void disclosureGroupAllFields() {
        DisclosureGroup dg = new DisclosureGroup();
        dg.setAcctGroupId("GRP001");
        dg.setTypeCd("01");
        dg.setCatCd(1);
        dg.setIntRate(new BigDecimal("15.00"));

        assertEquals("GRP001", dg.getAcctGroupId());
        assertEquals("01", dg.getTypeCd());
        assertEquals(1, dg.getCatCd());
        assertEquals(new BigDecimal("15.00"), dg.getIntRate());
    }

    @Test
    void dailyTransactionIdEquality() {
        DailyTransactionId id1 = new DailyTransactionId("T1", "C1", "TS1");
        DailyTransactionId id2 = new DailyTransactionId("T1", "C1", "TS1");
        DailyTransactionId id3 = new DailyTransactionId("T2", "C1", "TS1");

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
        assertNotEquals(id1, null);
        assertEquals(id1, id1);
    }

    @Test
    void dailyTransactionIdGettersSetters() {
        DailyTransactionId id = new DailyTransactionId();
        id.setTranId("T1");
        id.setCardNum("C1");
        id.setOrigTs("TS1");
        assertEquals("T1", id.getTranId());
        assertEquals("C1", id.getCardNum());
        assertEquals("TS1", id.getOrigTs());
    }

    @Test
    void tranCatBalanceIdEquality() {
        TranCatBalanceId id1 = new TranCatBalanceId(1L, "01", 1);
        TranCatBalanceId id2 = new TranCatBalanceId(1L, "01", 1);
        TranCatBalanceId id3 = new TranCatBalanceId(2L, "01", 1);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
        assertNotEquals(id1, null);
        assertEquals(id1, id1);
    }

    @Test
    void tranCatBalanceIdGettersSetters() {
        TranCatBalanceId id = new TranCatBalanceId();
        id.setAcctId(99L);
        id.setTypeCd("02");
        id.setCatCd(3);
        assertEquals(99L, id.getAcctId());
        assertEquals("02", id.getTypeCd());
        assertEquals(3, id.getCatCd());
    }

    @Test
    void disclosureGroupIdEquality() {
        DisclosureGroupId id1 = new DisclosureGroupId("G1", "01", 1);
        DisclosureGroupId id2 = new DisclosureGroupId("G1", "01", 1);
        DisclosureGroupId id3 = new DisclosureGroupId("G2", "01", 1);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
        assertNotEquals(id1, null);
        assertEquals(id1, id1);
    }

    @Test
    void disclosureGroupIdGettersSetters() {
        DisclosureGroupId id = new DisclosureGroupId();
        id.setAcctGroupId("GRP");
        id.setTypeCd("03");
        id.setCatCd(5);
        assertEquals("GRP", id.getAcctGroupId());
        assertEquals("03", id.getTypeCd());
        assertEquals(5, id.getCatCd());
    }

    @Test
    void tranCategoryIdEquality() {
        TranCategoryId id1 = new TranCategoryId("01", 1);
        TranCategoryId id2 = new TranCategoryId("01", 1);
        TranCategoryId id3 = new TranCategoryId("02", 1);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
        assertNotEquals(id1, null);
        assertEquals(id1, id1);
    }

    @Test
    void tranCategoryIdGettersSetters() {
        TranCategoryId id = new TranCategoryId();
        id.setTypeCd("04");
        id.setCatCd(7);
        assertEquals("04", id.getTypeCd());
        assertEquals(7, id.getCatCd());
    }

    @Test
    void dailyTransactionIdNotEqualToDifferentType() {
        DailyTransactionId id = new DailyTransactionId("T1", "C1", "TS1");
        assertNotEquals(id, "string");
    }

    @Test
    void tranCatBalanceIdNotEqualToDifferentType() {
        TranCatBalanceId id = new TranCatBalanceId(1L, "01", 1);
        assertNotEquals(id, "string");
    }

    @Test
    void disclosureGroupIdNotEqualToDifferentType() {
        DisclosureGroupId id = new DisclosureGroupId("G1", "01", 1);
        assertNotEquals(id, "string");
    }

    @Test
    void tranCategoryIdNotEqualToDifferentType() {
        TranCategoryId id = new TranCategoryId("01", 1);
        assertNotEquals(id, "string");
    }
}
