package com.carddemo.harness;

import com.carddemo.harness.codec.PackedDecimalCodec;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PackedDecimalCodecTest {

    @Test
    void decodePositiveValue() {
        // Value: +2525.00 → unscaled 252500, 13 digits: 0000000252500
        // PIC S9(10)V99 COMP-3 = 7 bytes (13 data nibbles + 1 sign nibble)
        // Packed: 00 00 00 02 52 50 0C
        byte[] packed = new byte[]{0x00, 0x00, 0x00, 0x02, 0x52, 0x50, 0x0C};
        BigDecimal result = PackedDecimalCodec.decode(packed, 2);
        assertEquals(new BigDecimal("2525.00"), result);
    }

    @Test
    void decodeNegativeValue() {
        // Value: -2500.00 → unscaled 250000, 13 digits: 0000000250000
        // Packed: 00 00 00 02 50 00 0D
        byte[] packed = new byte[]{0x00, 0x00, 0x00, 0x02, 0x50, 0x00, 0x0D};
        BigDecimal result = PackedDecimalCodec.decode(packed, 2);
        assertEquals(new BigDecimal("-2500.00"), result);
    }

    @Test
    void decodeUnsignedValue() {
        // Value: 1005.00 → unscaled 100500, 13 digits: 0000000100500
        // Packed: 00 00 00 01 00 50 0F
        byte[] packed = new byte[]{0x00, 0x00, 0x00, 0x01, 0x00, 0x50, 0x0F};
        BigDecimal result = PackedDecimalCodec.decode(packed, 2);
        assertEquals(new BigDecimal("1005.00"), result);
    }

    @Test
    void decodeZero() {
        byte[] packed = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0C};
        BigDecimal result = PackedDecimalCodec.decode(packed, 2);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void decodeSmallPositive() {
        // Value: +1.23, PIC S9(3)V99, 3 bytes
        // Digits: 00123C → bytes: 0x00, 0x12, 0x3C
        byte[] packed = new byte[]{0x00, 0x12, 0x3C};
        BigDecimal result = PackedDecimalCodec.decode(packed, 2);
        assertEquals(new BigDecimal("1.23"), result);
    }

    @Test
    void encodeAndDecodeRoundTrip() {
        BigDecimal original = new BigDecimal("2525.00");
        byte[] encoded = PackedDecimalCodec.encode(original, 7, 2);
        BigDecimal decoded = PackedDecimalCodec.decode(encoded, 2);
        assertEquals(0, original.compareTo(decoded));
    }

    @Test
    void encodeNegativeRoundTrip() {
        BigDecimal original = new BigDecimal("-2500.00");
        byte[] encoded = PackedDecimalCodec.encode(original, 7, 2);
        BigDecimal decoded = PackedDecimalCodec.decode(encoded, 2);
        assertEquals(0, original.compareTo(decoded));
    }

    @Test
    void encodeZeroRoundTrip() {
        BigDecimal original = BigDecimal.ZERO;
        byte[] encoded = PackedDecimalCodec.encode(original, 7, 2);
        BigDecimal decoded = PackedDecimalCodec.decode(encoded, 2);
        assertEquals(0, original.compareTo(decoded));
    }

    @Test
    void encodeLargeValueRoundTrip() {
        BigDecimal original = new BigDecimal("9999999999.99");
        byte[] encoded = PackedDecimalCodec.encode(original, 7, 2);
        BigDecimal decoded = PackedDecimalCodec.decode(encoded, 2);
        assertEquals(0, original.compareTo(decoded));
    }

    @Test
    void decodeAllZeroBytes() {
        byte[] packed = new byte[7]; // all zeros, last nibble = 0x00 (not std sign, but zero)
        BigDecimal result = PackedDecimalCodec.decode(packed, 2);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }
}
