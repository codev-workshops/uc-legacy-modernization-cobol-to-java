package com.carddemo.batch.cbact01c.codec;

import static com.carddemo.batch.cbact01c.codec.CobolCharset.ASCII;
import static com.carddemo.batch.cbact01c.codec.CobolCharset.EBCDIC;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class ZonedDecimalCodecTest {

    private static byte[] ascii(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    /** PIC S9(10)V99: 10 integer digits, 2 decimals, so "00000001940{" (first sample balance) is 194.00. */
    @Test
    void encodesPositiveWithCZone() {
        assertArrayEquals(ascii("00000001940{"),
                ZonedDecimalCodec.encodeSigned(new BigDecimal("194.00"), 10, 2, ASCII));
        assertArrayEquals(Hex.bytes("F0F0F0F0F0F0F0F1F9F4F0C0"),
                ZonedDecimalCodec.encodeSigned(new BigDecimal("194.00"), 10, 2, EBCDIC));
        assertArrayEquals(ascii("00000019400{"),
                ZonedDecimalCodec.encodeSigned(new BigDecimal("1940.00"), 10, 2, ASCII));
        assertArrayEquals(Hex.bytes("F0F0F0F0F0F0F1F9F4F0F0C0"),
                ZonedDecimalCodec.encodeSigned(new BigDecimal("1940.00"), 10, 2, EBCDIC));
    }

    @Test
    void encodesZeroWithCZone() {
        assertArrayEquals(ascii("00000000000{"), ZonedDecimalCodec.encodeSigned(BigDecimal.ZERO, 10, 2, ASCII));
        assertArrayEquals(Hex.bytes("F0F0F0F0F0F0F0F0F0F0F0C0"),
                ZonedDecimalCodec.encodeSigned(new BigDecimal("0.00"), 10, 2, EBCDIC));
    }

    @Test
    void encodesNegativeWithDZone() {
        assertArrayEquals(ascii("00000010250}"),
                ZonedDecimalCodec.encodeSigned(new BigDecimal("-1025.00"), 10, 2, ASCII));
        assertArrayEquals(Hex.bytes("F0F0F0F0F0F0F1F0F2F5F0D0"),
                ZonedDecimalCodec.encodeSigned(new BigDecimal("-1025.00"), 10, 2, EBCDIC));
        assertArrayEquals(ascii("00000102500}"),
                ZonedDecimalCodec.encodeSigned(new BigDecimal("-10250.00"), 10, 2, ASCII));
        assertArrayEquals(ascii("00000000012K"),
                ZonedDecimalCodec.encodeSigned(new BigDecimal("-1.22"), 10, 2, ASCII));
    }

    @Test
    void decodesSignedInBothCharsets() {
        assertEquals(new BigDecimal("194.00"),
                ZonedDecimalCodec.decodeSigned(ascii("00000001940{"), 0, 10, 2, ASCII));
        assertEquals(new BigDecimal("194.00"),
                ZonedDecimalCodec.decodeSigned(Hex.bytes("F0F0F0F0F0F0F0F1F9F4F0C0"), 0, 10, 2, EBCDIC));
        assertEquals(new BigDecimal("-1025.00"),
                ZonedDecimalCodec.decodeSigned(ascii("00000010250}"), 0, 10, 2, ASCII));
        assertEquals(new BigDecimal("-1025.09"),
                ZonedDecimalCodec.decodeSigned(ascii("xx00000010250R"), 2, 10, 2, ASCII));
    }

    @Test
    void decodeAcceptsFZoneOnSignedField() {
        assertEquals(new BigDecimal("194.00"),
                ZonedDecimalCodec.decodeSigned(ascii("000000019400"), 0, 10, 2, ASCII));
        assertEquals(new BigDecimal("194.00"),
                ZonedDecimalCodec.decodeSigned(Hex.bytes("F0F0F0F0F0F0F0F1F9F4F0F0"), 0, 10, 2, EBCDIC));
    }

    @Test
    void unsignedPic9of11() {
        assertArrayEquals(ascii("00000000001"), ZonedDecimalCodec.encodeUnsigned(1L, 11, ASCII));
        assertArrayEquals(Hex.bytes("F0F0F0F0F0F0F0F0F0F0F1"), ZonedDecimalCodec.encodeUnsigned(1L, 11, EBCDIC));
        assertArrayEquals(Hex.bytes("F0F0F0F0F0F0F0F0F0F5F0"),
                ZonedDecimalCodec.encodeUnsigned("00000000050", EBCDIC));
        assertEquals("00000000050",
                ZonedDecimalCodec.decodeUnsigned(Hex.bytes("F0F0F0F0F0F0F0F0F0F5F0"), 0, 11, EBCDIC));
        assertEquals("00000000001", ZonedDecimalCodec.decodeUnsigned(ascii("00000000001"), 0, 11, ASCII));
    }

    @Test
    void truncatesOnTheLeftLikeCobolMove() {
        assertArrayEquals(ascii("23456789012C"),
                ZonedDecimalCodec.encodeSigned(new BigDecimal("12345678901.23"), 10, 2, ASCII));
        assertArrayEquals(ascii("00000000001"), ZonedDecimalCodec.encodeUnsigned(100000000001L, 11, ASCII));
    }

    @Test
    void rejectsGarbage() {
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalCodec.decodeSigned(ascii("0000000194 {"), 0, 10, 2, ASCII));
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalCodec.decodeUnsigned(ascii("0000000000A"), 0, 11, ASCII));
        assertThrows(IllegalArgumentException.class, () -> ZonedDecimalCodec.encodeUnsigned("12-4", ASCII));
    }
}
