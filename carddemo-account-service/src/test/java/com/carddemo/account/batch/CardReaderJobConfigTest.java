package com.carddemo.account.batch;

import com.carddemo.account.entity.Card;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardReaderJobConfigTest {

    @Test
    void testFormatCardFixedWidth() {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .cardAcctId(12345678901L)
                .cardCvvCd(123)
                .cardEmbossedName("JOHN DOE")
                .cardExpirationDate("2025-12-31")
                .cardActiveStatus("Y")
                .build();

        String result = CardReaderJobConfig.formatCard(card);
        assertEquals(150, result.length());
        assertEquals("4111111111111111", result.substring(0, 16));
        assertEquals("12345678901", result.substring(16, 27));
        assertEquals("123", result.substring(27, 30));
        assertEquals("Y", result.substring(90, 91));
    }

    @Test
    void testFormatCardWithNulls() {
        Card card = Card.builder()
                .cardNum("1234567890123456")
                .cardAcctId(1L)
                .build();

        String result = CardReaderJobConfig.formatCard(card);
        assertEquals(150, result.length());
        assertEquals("1234567890123456", result.substring(0, 16));
        assertEquals("00000000001", result.substring(16, 27));
        assertEquals("000", result.substring(27, 30));
    }

    @Test
    void testFormatCardEmbossedNameTruncation() {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .cardAcctId(1L)
                .cardCvvCd(999)
                .cardEmbossedName("A VERY LONG NAME THAT EXCEEDS FIFTY CHARACTERS AND KEEPS GOING")
                .cardExpirationDate("2025-12-31")
                .cardActiveStatus("N")
                .build();

        String result = CardReaderJobConfig.formatCard(card);
        assertEquals(150, result.length());
    }
}
