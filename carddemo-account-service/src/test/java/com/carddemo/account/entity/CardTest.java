package com.carddemo.account.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Account account = Account.builder().acctId(1L).build();
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .account(account)
                .cardAcctId(1L)
                .cardCvvCd(123)
                .cardEmbossedName("JOHN DOE")
                .cardExpirationDate("2025-12-31")
                .cardActiveStatus("Y")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals("4111111111111111", card.getCardNum());
        assertEquals(account, card.getAccount());
        assertEquals(1L, card.getCardAcctId());
        assertEquals(123, card.getCardCvvCd());
        assertEquals("JOHN DOE", card.getCardEmbossedName());
        assertEquals("2025-12-31", card.getCardExpirationDate());
        assertEquals("Y", card.getCardActiveStatus());
        assertEquals(now, card.getCreatedAt());
        assertEquals(now, card.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Card card = new Card();
        assertNull(card.getCardNum());
        assertNull(card.getCardAcctId());
    }

    @Test
    void testEqualsAndHashCode() {
        Card c1 = Card.builder().cardNum("4111111111111111").cardAcctId(1L).build();
        Card c2 = Card.builder().cardNum("4111111111111111").cardAcctId(1L).build();
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
