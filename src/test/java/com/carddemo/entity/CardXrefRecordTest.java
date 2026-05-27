package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardXrefRecordTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CVACT03Y.cpy has 3 non-FILLER fields
        assertEquals(3, CardXrefRecord.class.getRecordComponents().length);
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new CardXrefRecord("4111111111111111", 123456789, 12345678901L);

        assertEquals("4111111111111111", record.xrefCardNum());
        assertEquals(123456789, record.xrefCustId());
        assertEquals(12345678901L, record.xrefAcctId());
    }
}
