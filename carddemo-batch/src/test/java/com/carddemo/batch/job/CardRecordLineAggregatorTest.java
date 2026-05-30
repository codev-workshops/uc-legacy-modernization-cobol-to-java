package com.carddemo.batch.job;

import com.carddemo.common.entity.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardRecordLineAggregatorTest {

    private CardRecordLineAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new CardRecordLineAggregator();
    }

    @Test
    void aggregateProducesCorrectFixedWidthLine() {
        Card card = new Card();
        card.setCardNum("0500024453765740");
        card.setAcctId(50747L);
        card.setCvvCd(123);
        card.setEmbossedName("Aniya Von");
        card.setExpirationDate("2023-03-09");
        card.setActiveStatus("Y");

        String result = aggregator.aggregate(card);

        assertEquals(150, result.length());
        assertEquals("0500024453765740", result.substring(0, 16));
        assertEquals("00000050747", result.substring(16, 27));
        assertEquals("123", result.substring(27, 30));
        assertEquals("Aniya Von", result.substring(30, 39));
        assertEquals("2023-03-09", result.substring(80, 90));
        assertEquals("Y", result.substring(90, 91));
    }

    @Test
    void aggregateHandlesNullFields() {
        Card card = new Card();
        card.setCardNum("1234567890123456");
        card.setAcctId(null);
        card.setCvvCd(null);
        card.setEmbossedName(null);
        card.setExpirationDate(null);
        card.setActiveStatus(null);

        String result = aggregator.aggregate(card);

        assertEquals(150, result.length());
        assertEquals("1234567890123456", result.substring(0, 16));
        assertEquals("00000000000", result.substring(16, 27));
        assertEquals("000", result.substring(27, 30));
    }

    @Test
    void aggregateHandlesMaxLengthFields() {
        Card card = new Card();
        card.setCardNum("1234567890123456");
        card.setAcctId(99999999999L);
        card.setCvvCd(999);
        card.setEmbossedName("A very long name that exceeds fifty characters limit here");
        card.setExpirationDate("2025-12-31");
        card.setActiveStatus("Y");

        String result = aggregator.aggregate(card);

        assertEquals(150, result.length());
        assertEquals("99999999999", result.substring(16, 27));
        assertEquals("999", result.substring(27, 30));
        // Name truncated to 50 chars
        assertEquals(50, result.substring(30, 80).length());
    }
}
