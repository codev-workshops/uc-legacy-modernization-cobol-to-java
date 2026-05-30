package com.carddemo.batch.job.xrefprinter;

import com.carddemo.common.entity.CardXref;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XrefLineAggregatorTest {

    private final XrefLineAggregator aggregator = new XrefLineAggregator();

    @Test
    void aggregateProducesFixedWidthRecord() {
        CardXref xref = buildXref("0500024453765740", 50L, 50L);

        String line = aggregator.aggregate(xref);

        assertEquals(XrefLineAggregator.RECORD_LEN, line.length());
        assertEquals("0500024453765740", line.substring(0, 16));
        assertEquals("000000050", line.substring(16, 25));
        assertEquals("00000000050", line.substring(25, 36));
        assertEquals("              ", line.substring(36, 50));
    }

    @Test
    void aggregateWithLargeIds() {
        CardXref xref = buildXref("9999999999999999", 999999999L, 99999999999L);

        String line = aggregator.aggregate(xref);

        assertEquals(XrefLineAggregator.RECORD_LEN, line.length());
        assertEquals("9999999999999999", line.substring(0, 16));
        assertEquals("999999999", line.substring(16, 25));
        assertEquals("99999999999", line.substring(25, 36));
    }

    @Test
    void aggregateWithZeroIds() {
        CardXref xref = buildXref("0000000000000000", 0L, 0L);

        String line = aggregator.aggregate(xref);

        assertEquals("000000000000000000000000000000000000              ", line);
    }

    @Test
    void aggregateWithShortCardNum() {
        CardXref xref = buildXref("1234", 1L, 2L);

        String line = aggregator.aggregate(xref);

        assertEquals(XrefLineAggregator.RECORD_LEN, line.length());
        assertEquals("1234            ", line.substring(0, 16));
    }

    @Test
    void aggregateWithNullCardNum() {
        CardXref xref = buildXref(null, 1L, 1L);

        String line = aggregator.aggregate(xref);

        assertEquals(XrefLineAggregator.RECORD_LEN, line.length());
        assertEquals("                ", line.substring(0, 16));
    }

    @Test
    void aggregateWithNullIds() {
        CardXref xref = new CardXref();
        xref.setXrefCardNum("0500024453765740");

        String line = aggregator.aggregate(xref);

        assertEquals(XrefLineAggregator.RECORD_LEN, line.length());
        assertEquals("000000000", line.substring(16, 25));
        assertEquals("00000000000", line.substring(25, 36));
    }

    @Test
    void padRightPadsShorterStrings() {
        assertEquals("ABC   ", XrefLineAggregator.padRight("ABC", 6));
    }

    @Test
    void padRightTruncatesLongerStrings() {
        assertEquals("ABCDEF", XrefLineAggregator.padRight("ABCDEFGHIJ", 6));
    }

    @Test
    void padRightHandlesExactLength() {
        assertEquals("ABCDEF", XrefLineAggregator.padRight("ABCDEF", 6));
    }

    @Test
    void zeroPadFormatsCorrectly() {
        assertEquals("007", XrefLineAggregator.zeroPad(7L, 3));
        assertEquals("000", XrefLineAggregator.zeroPad(0L, 3));
        assertEquals("123", XrefLineAggregator.zeroPad(123L, 3));
    }

    @Test
    void zeroPadHandlesNull() {
        assertEquals("000", XrefLineAggregator.zeroPad(null, 3));
    }

    private CardXref buildXref(String cardNum, Long custId, Long acctId) {
        CardXref xref = new CardXref();
        xref.setXrefCardNum(cardNum);
        xref.setCustId(custId);
        xref.setAcctId(acctId);
        return xref;
    }
}
