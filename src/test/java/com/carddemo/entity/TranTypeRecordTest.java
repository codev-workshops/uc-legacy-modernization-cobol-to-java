package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranTypeRecordTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CVTRA03Y.cpy has 2 non-FILLER fields
        assertEquals(2, TranTypeRecord.class.getRecordComponents().length);
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new TranTypeRecord("SA", "Sale");

        assertEquals("SA", record.tranType());
        assertEquals("Sale", record.tranTypeDesc());
    }
}
