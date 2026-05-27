package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TranCatBalRecordTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CVTRA01Y.cpy has 4 non-FILLER fields (3 key + 1 balance)
        assertEquals(4, TranCatBalRecord.class.getRecordComponents().length);
    }

    @Test
    void balanceFieldIsBigDecimal() {
        var record = new TranCatBalRecord(12345678901L, "SA", 5001, new BigDecimal("1500.75"));
        assertInstanceOf(BigDecimal.class, record.tranCatBal());
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new TranCatBalRecord(12345678901L, "SA", 5001, new BigDecimal("1500.75"));

        assertEquals(12345678901L, record.trancatAcctId());
        assertEquals("SA", record.trancatTypeCd());
        assertEquals(5001, record.trancatCd());
        assertEquals(new BigDecimal("1500.75"), record.tranCatBal());
    }
}
