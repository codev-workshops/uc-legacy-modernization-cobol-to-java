package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DiscGroupRecordTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CVTRA02Y.cpy has 4 non-FILLER fields (3 key + 1 rate)
        assertEquals(4, DiscGroupRecord.class.getRecordComponents().length);
    }

    @Test
    void interestRateIsBigDecimal() {
        var record = new DiscGroupRecord("GRP001", "SA", 5001, new BigDecimal("18.99"));
        assertInstanceOf(BigDecimal.class, record.disIntRate());
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new DiscGroupRecord("GRP001", "SA", 5001, new BigDecimal("18.99"));

        assertEquals("GRP001", record.disAcctGroupId());
        assertEquals("SA", record.disTranTypeCd());
        assertEquals(5001, record.disTranCatCd());
        assertEquals(new BigDecimal("18.99"), record.disIntRate());
    }
}
