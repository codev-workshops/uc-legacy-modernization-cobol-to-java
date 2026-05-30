package com.carddemo.account.batch;

import com.carddemo.account.entity.CardXref;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XrefReaderJobConfigTest {

    @Test
    void testFormatXrefFixedWidth() {
        CardXref xref = CardXref.builder()
                .xrefCardNum("4111111111111111")
                .xrefCustId(123456789L)
                .xrefAcctId(12345678901L)
                .build();

        String result = XrefReaderJobConfig.formatXref(xref);
        assertEquals(50, result.length());
        assertEquals("4111111111111111", result.substring(0, 16));
        assertEquals("123456789", result.substring(16, 25));
        assertEquals("12345678901", result.substring(25, 36));
    }

    @Test
    void testFormatXrefWithSmallIds() {
        CardXref xref = CardXref.builder()
                .xrefCardNum("1234567890123456")
                .xrefCustId(1L)
                .xrefAcctId(1L)
                .build();

        String result = XrefReaderJobConfig.formatXref(xref);
        assertEquals(50, result.length());
        assertEquals("000000001", result.substring(16, 25));
        assertEquals("00000000001", result.substring(25, 36));
    }
}
