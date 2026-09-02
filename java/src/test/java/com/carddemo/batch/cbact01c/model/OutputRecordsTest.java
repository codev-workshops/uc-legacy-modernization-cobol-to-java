package com.carddemo.batch.cbact01c.model;

import static com.carddemo.batch.cbact01c.codec.CobolCharset.ASCII;
import static com.carddemo.batch.cbact01c.codec.CobolCharset.EBCDIC;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.carddemo.batch.cbact01c.codec.Hex;
import com.carddemo.batch.cbact01c.model.ArrArrayRec.ArrAcctBal;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class OutputRecordsTest {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    @Test
    void initializedArrArrayRecMatchesCobolInitialize() {
        byte[] bytes = ArrArrayRec.initialized().toBytes(EBCDIC);
        assertEquals(110, bytes.length);
        StringBuilder expected = new StringBuilder("F0".repeat(11));
        for (int i = 0; i < 5; i++) {
            expected.append("F0".repeat(11)).append("C0").append("0000000000000C");
        }
        expected.append("40".repeat(4));
        assertEquals(expected.toString(), Hex.of(bytes));
    }

    @Test
    void initializedArrArrayRecInAscii() {
        byte[] bytes = ArrArrayRec.initialized().toBytes(ASCII);
        assertEquals(110, bytes.length);
        assertEquals("00000000000" + "00000000000{", new String(bytes, 0, 23, ASCII.charset()));
        assertEquals("    ", new String(bytes, 106, 4, ASCII.charset()));
    }

    @Test
    void arrArrayRecRoundTripsWithPopulatedOccurrences() {
        ArrArrayRec rec = ArrArrayRec.initialized()
                .withAcctId("00000000001")
                .withBal(0, new ArrAcctBal(new BigDecimal("1940.00"), new BigDecimal("1005.00")))
                .withBal(1, new ArrAcctBal(new BigDecimal("1940.00"), new BigDecimal("1525.00")))
                .withBal(2, new ArrAcctBal(new BigDecimal("-1025.00"), new BigDecimal("-2500.00")));
        byte[] bytes = rec.toBytes(EBCDIC);
        assertEquals(110, bytes.length);
        assertEquals("F0F0F0F0F0F0F1F9F4F0F0C0" + "0000000100500C", Hex.of(Arrays.copyOfRange(bytes, 11, 30)));
        assertEquals("F0F0F0F0F0F0F1F0F2F5F0D0" + "0000000250000D", Hex.of(Arrays.copyOfRange(bytes, 49, 68)));
        assertEquals(rec, ArrArrayRec.fromBytes(bytes, EBCDIC));
        assertEquals(ArrAcctBal.initialized(), rec.bal(4));
    }

    @Test
    void arrArrayRecRequiresFiveOccurrences() {
        assertThrows(IllegalArgumentException.class,
                () -> new ArrArrayRec("00000000000", new ArrAcctBal[4], "    "));
    }

    @Test
    void outAcctRecIs107BytesAndRoundTrips() {
        OutAcctRec rec = new OutAcctRec("00000000001", "Y", new BigDecimal("1940.00"), new BigDecimal("20200.00"),
                new BigDecimal("10200.00"), "2014-11-20", "2025-05-20", "20250520  ", ZERO,
                new BigDecimal("2525.00"), "          ");
        byte[] bytes = rec.toBytes(EBCDIC);
        assertEquals(107, bytes.length);
        assertArrayEquals(Hex.bytes("00 00 00 02 52 50 0C"), Arrays.copyOfRange(bytes, 90, 97));
        assertEquals(rec, OutAcctRec.fromBytes(bytes, EBCDIC));
        assertEquals(107, rec.toBytes(ASCII).length);
        assertEquals(rec, OutAcctRec.fromBytes(rec.toBytes(ASCII), ASCII));
    }

    @Test
    void vb1RecIs12BytesAndRoundTrips() {
        Vb1Rec rec = new Vb1Rec("00000000001", "Y");
        byte[] bytes = rec.toBytes(EBCDIC);
        assertEquals(12, bytes.length);
        assertEquals("F0F0F0F0F0F0F0F0F0F0F1E8", Hex.of(bytes));
        assertEquals(rec, Vb1Rec.fromBytes(bytes, EBCDIC));
        assertEquals("00000000001Y", new String(rec.toBytes(ASCII), ASCII.charset()));
    }

    @Test
    void vb2RecIs39BytesAndRoundTrips() {
        Vb2Rec rec = new Vb2Rec("00000000001", new BigDecimal("194.00"), new BigDecimal("2020.00"), "2025");
        byte[] bytes = rec.toBytes(EBCDIC);
        assertEquals(39, bytes.length);
        assertEquals(rec, Vb2Rec.fromBytes(bytes, EBCDIC));
        assertEquals("00000000001" + "00000001940{" + "00000020200{" + "2025",
                new String(rec.toBytes(ASCII), ASCII.charset()));
    }
}
