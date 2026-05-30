package com.carddemo.common.codec;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PackedDecimalCodecTest {

    @Test
    void decodePositiveZero() {
        // 0x0C = positive zero
        byte[] data = {0x0C};
        assertEquals(BigDecimal.ZERO, PackedDecimalCodec.decode(data, 0));
    }

    @Test
    void decodeNegativeZero() {
        byte[] data = {0x0D};
        BigDecimal result = PackedDecimalCodec.decode(data, 0);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void decodePositiveInteger() {
        // 0x12, 0x3C = +123
        byte[] data = {0x12, 0x3C};
        assertEquals(new BigDecimal("123"), PackedDecimalCodec.decode(data, 0));
    }

    @Test
    void decodeNegativeInteger() {
        // 0x12, 0x3D = -123
        byte[] data = {0x12, 0x3D};
        assertEquals(new BigDecimal("-123"), PackedDecimalCodec.decode(data, 0));
    }

    @Test
    void decodeWithScale() {
        // PIC S9(10)V99 COMP-3 => 7 bytes
        // 1940.00 => 194000 => 0x00 0x00 0x00 0x01 0x94 0x00 0x0C
        byte[] data = {0x00, 0x00, 0x00, 0x01, (byte) 0x94, 0x00, 0x0C};
        BigDecimal result = PackedDecimalCodec.decode(data, 2);
        assertEquals(0, new BigDecimal("1940.00").compareTo(result));
    }

    @Test
    void decodeNegativeWithScale() {
        // -1025.02 => 102502 => 0x00 0x00 0x00 0x01 0x02 0x50 0x2D
        byte[] data = {0x00, 0x00, 0x00, 0x01, 0x02, 0x50, 0x2D};
        BigDecimal result = PackedDecimalCodec.decode(data, 2);
        assertEquals(0, new BigDecimal("-1025.02").compareTo(result));
    }

    @Test
    void decodeEmptyReturnsZero() {
        assertEquals(BigDecimal.ZERO, PackedDecimalCodec.decode(new byte[0], 0));
    }

    @Test
    void decodeWithOffset() {
        byte[] full = {(byte) 0xFF, (byte) 0xFF, 0x12, 0x3C, (byte) 0xFF};
        BigDecimal result = PackedDecimalCodec.decode(full, 2, 2, 0);
        assertEquals(new BigDecimal("123"), result);
    }

    @Test
    void encodePositive() {
        byte[] encoded = PackedDecimalCodec.encode(new BigDecimal("123"), 2, 0);
        assertArrayEquals(new byte[]{0x12, 0x3C}, encoded);
    }

    @Test
    void encodeNegative() {
        byte[] encoded = PackedDecimalCodec.encode(new BigDecimal("-123"), 2, 0);
        assertArrayEquals(new byte[]{0x12, 0x3D}, encoded);
    }

    @Test
    void encodeWithScale() {
        byte[] encoded = PackedDecimalCodec.encode(new BigDecimal("1940.00"), 7, 2);
        byte[] expected = {0x00, 0x00, 0x00, 0x01, (byte) 0x94, 0x00, 0x0C};
        assertArrayEquals(expected, encoded);
    }

    @Test
    void encodeExceedsCapacityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> PackedDecimalCodec.encode(new BigDecimal("9999999999999999"), 2, 0));
    }

    @Test
    void roundTrip() {
        BigDecimal original = new BigDecimal("25525.99");
        byte[] encoded = PackedDecimalCodec.encode(original, 7, 2);
        BigDecimal decoded = PackedDecimalCodec.decode(encoded, 2);
        assertEquals(0, original.compareTo(decoded));
    }

    @Test
    void roundTripNegative() {
        BigDecimal original = new BigDecimal("-9999.50");
        byte[] encoded = PackedDecimalCodec.encode(original, 7, 2);
        BigDecimal decoded = PackedDecimalCodec.decode(encoded, 2);
        assertEquals(0, original.compareTo(decoded));
    }

    @Test
    void unsignedDecode() {
        // 0x0F = unsigned
        byte[] data = {0x12, 0x3F};
        BigDecimal result = PackedDecimalCodec.decode(data, 0);
        assertEquals(new BigDecimal("123"), result);
    }
}
