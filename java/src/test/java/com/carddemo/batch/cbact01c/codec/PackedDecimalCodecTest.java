package com.carddemo.batch.cbact01c.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PackedDecimalCodecTest {

    @Test
    void byteLengthOfS9of10V99IsSeven() {
        assertEquals(7, PackedDecimalCodec.byteLength(10, 2));
    }

    @Test
    void encodesDocumentedExamples() {
        assertArrayEquals(Hex.bytes("00 00 00 02 52 50 0C"), PackedDecimalCodec.encode(new BigDecimal("2525.00"), 10, 2));
        assertArrayEquals(Hex.bytes("00 00 00 02 50 00 0D"), PackedDecimalCodec.encode(new BigDecimal("-2500.00"), 10, 2));
        assertArrayEquals(Hex.bytes("00 00 00 00 00 00 0C"), PackedDecimalCodec.encode(BigDecimal.ZERO, 10, 2));
        assertArrayEquals(Hex.bytes("00 00 00 01 00 50 0C"), PackedDecimalCodec.encode(new BigDecimal("1005.00"), 10, 2));
        assertArrayEquals(Hex.bytes("00 00 00 01 52 50 0C"), PackedDecimalCodec.encode(new BigDecimal("1525.00"), 10, 2));
    }

    @Test
    void decodesDocumentedExamples() {
        assertEquals(new BigDecimal("2525.00"), PackedDecimalCodec.decode(Hex.bytes("00 00 00 02 52 50 0C"), 0, 10, 2));
        assertEquals(new BigDecimal("-2500.00"), PackedDecimalCodec.decode(Hex.bytes("00 00 00 02 50 00 0D"), 0, 10, 2));
        assertEquals(new BigDecimal("0.00"), PackedDecimalCodec.decode(Hex.bytes("00 00 00 00 00 00 0C"), 0, 10, 2));
        assertEquals(new BigDecimal("2525.00"),
                PackedDecimalCodec.decode(Hex.bytes("FF FF 00 00 00 02 52 50 0C"), 2, 10, 2));
    }

    @Test
    void decodeAcceptsFSign() {
        assertEquals(new BigDecimal("2525.00"), PackedDecimalCodec.decode(Hex.bytes("00 00 00 02 52 50 0F"), 0, 10, 2));
    }

    @Test
    void roundTrips() {
        for (String v : new String[] {"0.00", "0.01", "-0.01", "1940.00", "-1025.00", "9999999999.99", "-9999999999.99", "123.45"}) {
            BigDecimal value = new BigDecimal(v);
            assertEquals(value, PackedDecimalCodec.decode(PackedDecimalCodec.encode(value, 10, 2), 0, 10, 2), v);
        }
    }

    @Test
    void rejectsInvalidNibbles() {
        assertThrows(IllegalArgumentException.class,
                () -> PackedDecimalCodec.decode(Hex.bytes("00 00 00 02 52 50 0A"), 0, 10, 2));
        assertThrows(IllegalArgumentException.class,
                () -> PackedDecimalCodec.decode(Hex.bytes("00 00 00 0B 52 50 0C"), 0, 10, 2));
    }
}
