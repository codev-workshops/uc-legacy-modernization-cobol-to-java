package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranCatRecordTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CVTRA04Y.cpy has 3 non-FILLER fields (2 key + 1 desc)
        assertEquals(3, TranCatRecord.class.getRecordComponents().length);
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new TranCatRecord("SA", 5001, "Retail Purchase");

        assertEquals("SA", record.tranTypeCd());
        assertEquals(5001, record.tranCatCd());
        assertEquals("Retail Purchase", record.tranCatTypeDesc());
    }
}
