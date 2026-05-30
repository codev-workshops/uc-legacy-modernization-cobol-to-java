package com.carddemo.account.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CardXrefTest {

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        CardXref xref = CardXref.builder()
                .xrefCardNum("4111111111111111")
                .xrefCustId(123456789L)
                .xrefAcctId(12345678901L)
                .createdAt(now)
                .build();

        assertEquals("4111111111111111", xref.getXrefCardNum());
        assertEquals(123456789L, xref.getXrefCustId());
        assertEquals(12345678901L, xref.getXrefAcctId());
        assertEquals(now, xref.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        CardXref xref = new CardXref();
        assertNull(xref.getXrefCardNum());
        assertNull(xref.getXrefCustId());
    }

    @Test
    void testEqualsAndHashCode() {
        CardXref x1 = CardXref.builder().xrefCardNum("4111111111111111").xrefCustId(1L).xrefAcctId(1L).build();
        CardXref x2 = CardXref.builder().xrefCardNum("4111111111111111").xrefCustId(1L).xrefAcctId(1L).build();
        assertEquals(x1, x2);
        assertEquals(x1.hashCode(), x2.hashCode());
    }
}
