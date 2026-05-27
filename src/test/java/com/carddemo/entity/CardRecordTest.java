package com.carddemo.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CardRecordTest {

    @Test
    void fieldCountMatchesCopybook() {
        // CVACT02Y.cpy has 6 non-FILLER fields
        assertEquals(6, CardRecord.class.getDeclaredFields().length);
    }

    @Test
    void piiFieldsAreStringType() throws NoSuchFieldException {
        assertEquals(String.class, CardRecord.class.getDeclaredField("cardNum").getType());
        assertEquals(String.class, CardRecord.class.getDeclaredField("cardCvvCd").getType());
    }

    @Test
    void canInstantiateWithValidData() {
        var record = new CardRecord();
        record.setCardNum("4111111111111111");
        record.setCardAcctId(12345678901L);
        record.setCardCvvCd("123");
        record.setCardEmbossedName("JOHN DOE");
        record.setCardExpirationDate(LocalDate.of(2025, 12, 31));
        record.setCardActiveStatus("Y");

        assertEquals("4111111111111111", record.getCardNum());
        assertEquals(12345678901L, record.getCardAcctId());
        assertEquals("123", record.getCardCvvCd());
    }
}
