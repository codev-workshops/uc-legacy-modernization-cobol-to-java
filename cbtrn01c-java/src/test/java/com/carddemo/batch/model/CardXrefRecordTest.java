package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CardXrefRecord.fromFixedLength() parsing.
 * Test data derived from golden-files/cardxref.json.
 */
class CardXrefRecordTest {

    @Test
    void shouldParseFirstGoldenRecord() {
        // First record from cardxref.json: cardNum="0500024453765740", custId=50, acctId=50
        // Layout: cardNum(16) + custId(9) + acctId(11) + filler(14) = 50 bytes
        String record = "0500024453765740" + "000000050" + "00000000050" + "              ";
        assertEquals(50, record.length());

        CardXrefRecord xref = CardXrefRecord.fromFixedLength(record);

        assertEquals("0500024453765740", xref.cardNum());
        assertEquals(50L, xref.custId());
        assertEquals(50L, xref.acctId());
    }

    @Test
    void shouldParseSecondGoldenRecord() {
        // Second record: cardNum="0683586198171516", custId=27, acctId=27
        String record = "0683586198171516" + "000000027" + "00000000027" + "              ";

        CardXrefRecord xref = CardXrefRecord.fromFixedLength(record);

        assertEquals("0683586198171516", xref.cardNum());
        assertEquals(27L, xref.custId());
        assertEquals(27L, xref.acctId());
    }

    @Test
    void shouldHandleLargeIds() {
        String record = "9999999999999999" + "999999999" + "99999999999" + "              ";

        CardXrefRecord xref = CardXrefRecord.fromFixedLength(record);

        assertEquals("9999999999999999", xref.cardNum());
        assertEquals(999999999L, xref.custId());
        assertEquals(99999999999L, xref.acctId());
    }

    @Test
    void shouldRejectTooShortRecord() {
        assertThrows(IllegalArgumentException.class, () ->
                CardXrefRecord.fromFixedLength("short"));
    }
}
