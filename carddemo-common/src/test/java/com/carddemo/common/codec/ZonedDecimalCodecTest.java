package com.carddemo.common.codec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ZonedDecimalCodecTest {

    @Test
    void decodePositiveZero() {
        byte[] data = "{".getBytes(StandardCharsets.US_ASCII);
        assertEquals(BigDecimal.ZERO, ZonedDecimalCodec.decode(data, 0));
    }

    @Test
    void decodeNegativeZero() {
        byte[] data = "}".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 0);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void decodePositiveInteger() {
        // "123A" = +1231
        byte[] data = "123A".getBytes(StandardCharsets.US_ASCII);
        assertEquals(new BigDecimal("1231"), ZonedDecimalCodec.decode(data, 0));
    }

    @Test
    void decodeNegativeInteger() {
        // "123J" = -1231
        byte[] data = "123J".getBytes(StandardCharsets.US_ASCII);
        assertEquals(new BigDecimal("-1231"), ZonedDecimalCodec.decode(data, 0));
    }

    @Test
    void decodeWithScale() {
        // S9(10)V99: "00000019400{" = digits 000000194000, scale 2 => 1940.00
        byte[] data = "00000019400{".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 2);
        assertEquals(0, new BigDecimal("1940.00").compareTo(result));
    }

    @Test
    void decodeNegativeWithScale() {
        // "00000010250K" => digits 000000102502, negative, scale 2 => -1025.02
        byte[] data = "00000010250K".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 2);
        assertEquals(0, new BigDecimal("-1025.02").compareTo(result));
    }

    @Test
    void decodeEmptyReturnsZero() {
        assertEquals(BigDecimal.ZERO, ZonedDecimalCodec.decode(new byte[0], 0));
    }

    @Test
    void decodeWithOffset() {
        byte[] full = "XXXX123AYYYY".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(full, 4, 4, 0);
        assertEquals(new BigDecimal("1231"), result);
    }

    @Test
    void encodePositive() {
        byte[] encoded = ZonedDecimalCodec.encode(new BigDecimal("1940.00"), 12, 2);
        String s = new String(encoded, StandardCharsets.US_ASCII);
        assertEquals("00000019400{", s);
    }

    @Test
    void encodeNegative() {
        byte[] encoded = ZonedDecimalCodec.encode(new BigDecimal("-1025.02"), 12, 2);
        String s = new String(encoded, StandardCharsets.US_ASCII);
        assertEquals("00000010250K", s);
    }

    @Test
    void encodeExceedsLengthThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalCodec.encode(new BigDecimal("99999999999999"), 5, 0));
    }

    @ParameterizedTest
    @CsvSource({
        "'{', 1, 0",
        "'A', 1, 1",
        "'I', 1, 9",
        "'}', -1, 0",
        "'J', -1, 1",
        "'R', -1, 9",
        "'5', 1, 5"
    })
    void decodeOverpunch(char c, int expectedSign, int expectedDigit) {
        int[] result = ZonedDecimalCodec.decodeOverpunch(c);
        assertEquals(expectedSign, result[0]);
        assertEquals(expectedDigit, result[1]);
    }

    @Test
    void decodeOverpunchInvalidThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalCodec.decodeOverpunch('Z'));
    }

    @Test
    void roundTripAllPositiveDigits() {
        for (int d = 0; d <= 9; d++) {
            BigDecimal val = new BigDecimal(d);
            byte[] encoded = ZonedDecimalCodec.encode(val, 1, 0);
            BigDecimal decoded = ZonedDecimalCodec.decode(encoded, 0);
            assertEquals(0, val.compareTo(decoded),
                    "Round-trip failed for digit " + d);
        }
    }

    @Test
    void roundTripAllNegativeDigits() {
        for (int d = 0; d <= 9; d++) {
            BigDecimal val = new BigDecimal(-d);
            byte[] encoded = ZonedDecimalCodec.encode(val, 1, 0);
            BigDecimal decoded = ZonedDecimalCodec.decode(encoded, 0);
            assertEquals(0, val.compareTo(decoded),
                    "Round-trip failed for digit -" + d);
        }
    }
}
