package com.carddemo.transaction.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testTransactionBuilder() {
        Transaction t = Transaction.builder()
                .tranId("TRN001")
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranSource("ONLINE")
                .tranDesc("Test purchase")
                .tranAmt(new BigDecimal("100.50"))
                .tranMerchantId(12345L)
                .tranMerchantName("Test Merchant")
                .tranMerchantCity("New York")
                .tranMerchantZip("10001")
                .tranCardNum("4111111111111111")
                .tranOrigTs("2024-01-15-10.30.00.000000")
                .tranProcTs("2024-01-15-10.30.01.000000")
                .createdAt(LocalDateTime.now())
                .build();

        assertEquals("TRN001", t.getTranId());
        assertEquals("01", t.getTranTypeCd());
        assertEquals(1, t.getTranCatCd());
        assertEquals("ONLINE", t.getTranSource());
        assertEquals(new BigDecimal("100.50"), t.getTranAmt());
        assertEquals("4111111111111111", t.getTranCardNum());
    }

    @Test
    void testDailyTransactionBuilder() {
        DailyTransaction dt = DailyTransaction.builder()
                .dalytranId("DT001")
                .dalytranTypeCd("01")
                .dalytranCatCd(1)
                .dalytranSource("ONLINE")
                .dalytranDesc("Daily purchase")
                .dalytranAmt(new BigDecimal("50.00"))
                .dalytranMerchantId(99999L)
                .dalytranMerchantName("Merchant")
                .dalytranMerchantCity("City")
                .dalytranMerchantZip("12345")
                .dalytranCardNum("4222222222222222")
                .dalytranOrigTs("2024-01-15-10.30.00.000000")
                .build();

        assertEquals("DT001", dt.getDalytranId());
        assertEquals(new BigDecimal("50.00"), dt.getDalytranAmt());
    }

    @Test
    void testDailyRejectBuilder() {
        DailyReject dr = DailyReject.builder()
                .dalytranId("DT001")
                .rejectReason("Card expired")
                .rejectedAt(LocalDateTime.now())
                .build();

        assertEquals("DT001", dr.getDalytranId());
        assertEquals("Card expired", dr.getRejectReason());
        assertNull(dr.getRejectId());
    }

    @Test
    void testTranCatBalanceBuilder() {
        TranCatBalance tcb = TranCatBalance.builder()
                .trancatAcctId(1000000001L)
                .trancatTypeCd("01")
                .trancatCd(1)
                .tranCatBal(new BigDecimal("5000.00"))
                .build();

        assertEquals(1000000001L, tcb.getTrancatAcctId());
        assertEquals("01", tcb.getTrancatTypeCd());
        assertEquals(1, tcb.getTrancatCd());
        assertEquals(new BigDecimal("5000.00"), tcb.getTranCatBal());
    }

    @Test
    void testDisclosureGroupBuilder() {
        DisclosureGroup dg = DisclosureGroup.builder()
                .disAcctGroupId("GRP001")
                .disTranTypeCd("01")
                .disTranCatCd(1)
                .disIntRate(new BigDecimal("18.99"))
                .build();

        assertEquals("GRP001", dg.getDisAcctGroupId());
        assertEquals(new BigDecimal("18.99"), dg.getDisIntRate());
    }

    @Test
    void testTransactionTypeBuilder() {
        TransactionType tt = TransactionType.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        assertEquals("01", tt.getTranType());
        assertEquals("Purchase", tt.getTranTypeDesc());
    }

    @Test
    void testTransactionCategoryBuilder() {
        TransactionCategory tc = TransactionCategory.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Regular Sales Draft")
                .build();

        assertEquals("01", tc.getTranTypeCd());
        assertEquals(1, tc.getTranCatCd());
        assertEquals("Regular Sales Draft", tc.getTranCatTypeDesc());
    }

    @Test
    void testCompositeKeyIds() {
        TransactionCategoryId tcId = new TransactionCategoryId("01", 1);
        assertEquals("01", tcId.getTranTypeCd());
        assertEquals(1, tcId.getTranCatCd());

        TranCatBalanceId tcbId = new TranCatBalanceId(1000L, "01", 1);
        assertEquals(1000L, tcbId.getTrancatAcctId());

        DisclosureGroupId dgId = new DisclosureGroupId("GRP001", "01", 1);
        assertEquals("GRP001", dgId.getDisAcctGroupId());

        assertEquals(tcId, new TransactionCategoryId("01", 1));
        assertEquals(tcbId, new TranCatBalanceId(1000L, "01", 1));
        assertEquals(dgId, new DisclosureGroupId("GRP001", "01", 1));
    }

    @Test
    void testEqualsAndHashCode() {
        Transaction t1 = Transaction.builder().tranId("T1").build();
        Transaction t2 = Transaction.builder().tranId("T1").build();
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());

        DailyTransaction dt1 = DailyTransaction.builder().dalytranId("D1").build();
        DailyTransaction dt2 = DailyTransaction.builder().dalytranId("D1").build();
        assertEquals(dt1, dt2);
    }

    @Test
    void testNoArgsConstructor() {
        Transaction t = new Transaction();
        assertNull(t.getTranId());

        DailyTransaction dt = new DailyTransaction();
        assertNull(dt.getDalytranId());

        DailyReject dr = new DailyReject();
        assertNull(dr.getRejectId());

        TranCatBalance tcb = new TranCatBalance();
        assertNull(tcb.getTrancatAcctId());

        DisclosureGroup dg = new DisclosureGroup();
        assertNull(dg.getDisAcctGroupId());

        TransactionType tt = new TransactionType();
        assertNull(tt.getTranType());

        TransactionCategory tc = new TransactionCategory();
        assertNull(tc.getTranTypeCd());
    }

    @Test
    void testSetters() {
        Transaction t = new Transaction();
        t.setTranId("SET1");
        t.setTranTypeCd("02");
        t.setTranAmt(BigDecimal.TEN);
        assertEquals("SET1", t.getTranId());
        assertEquals("02", t.getTranTypeCd());
        assertEquals(BigDecimal.TEN, t.getTranAmt());
    }
}
