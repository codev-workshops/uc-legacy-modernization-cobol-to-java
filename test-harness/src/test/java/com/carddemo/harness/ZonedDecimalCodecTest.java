package com.carddemo.harness;

import com.carddemo.harness.codec.ZonedDecimalCodec;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ZonedDecimalCodecTest {

    @Test
    void decodePositiveZeroOverpunch() {
        // '{' = +0
        byte[] data = "000000000{".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 2);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void decodePositiveOverpunchA() {
        // 'A' = +1, unscaled value 11 in 10 chars = "000000001A" → digit string "0000000011" → 0.11
        byte[] data = "000000001A".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 2);
        assertEquals(new BigDecimal("0.11"), result);
    }

    @Test
    void decodePositiveOverpunchI() {
        // 'I' = +9, value 1940.09 → unscaled 194009, 10 chars: "000019400I"
        byte[] data = "000019400I".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 2);
        assertEquals(new BigDecimal("1940.09"), result);
    }

    @Test
    void decodeNegativeZeroOverpunch() {
        // '}' = -0
        byte[] data = "000000000}".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 2);
        assertEquals(0, result.compareTo(BigDecimal.ZERO.negate().setScale(2)));
    }

    @Test
    void decodeNegativeOverpunchJ() {
        // 'J' = -1, value -0.11 → unscaled 11, 10 chars: "000000001J"
        byte[] data = "000000001J".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 2);
        assertEquals(new BigDecimal("-0.11"), result);
    }

    @Test
    void decodeNegativeOverpunchR() {
        // 'R' = -9, value -2500.09 → unscaled 250009, 10 chars: "000025000R"
        byte[] data = "000025000R".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 2);
        assertEquals(new BigDecimal("-2500.09"), result);
    }

    @Test
    void decodeLargePositiveValue() {
        // PIC S9(10)V99 = 12 chars, value = 1940.00 → "000000194000" with overpunch on last '0' → '{'
        byte[] data = "00000019400{".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 2);
        assertEquals(new BigDecimal("1940.00"), result);
    }

    @Test
    void decodeLargeNegativeValue() {
        // value = -1025.00 → "000000102500" with overpunch: last '0' → '}'
        byte[] data = "00000010250}".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 2);
        assertEquals(new BigDecimal("-1025.00"), result);
    }

    @Test
    void decodeUnsignedDigits() {
        // Plain digits without overpunch (unsigned)
        byte[] data = "00000000001".getBytes(StandardCharsets.US_ASCII);
        BigDecimal result = ZonedDecimalCodec.decode(data, 0);
        assertEquals(new BigDecimal("1"), result);
    }

    @Test
    void encodeAndDecodeRoundTrip() {
        BigDecimal original = new BigDecimal("2525.00");
        byte[] encoded = ZonedDecimalCodec.encode(original, 12, 2);
        BigDecimal decoded = ZonedDecimalCodec.decode(encoded, 2);
        assertEquals(0, original.compareTo(decoded));
    }

    @Test
    void encodeNegativeRoundTrip() {
        BigDecimal original = new BigDecimal("-2500.00");
        byte[] encoded = ZonedDecimalCodec.encode(original, 12, 2);
        BigDecimal decoded = ZonedDecimalCodec.decode(encoded, 2);
        assertEquals(0, original.compareTo(decoded));
    }

    @Test
    void invalidOverpunchThrows() {
        byte[] data = "0000000000!".getBytes(StandardCharsets.US_ASCII);
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalCodec.decode(data, 2));
    }

    @Test
    void allPositiveOverpunchValues() {
        char[] overpunch = {'{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
        for (int digit = 0; digit <= 9; digit++) {
            byte[] data = ("0000" + overpunch[digit]).getBytes(StandardCharsets.US_ASCII);
            BigDecimal result = ZonedDecimalCodec.decode(data, 0);
            assertEquals(new BigDecimal(digit), result,
                    "Overpunch '" + overpunch[digit] + "' should decode to +" + digit);
        }
    }

    @Test
    void allNegativeOverpunchValues() {
        char[] overpunch = {'}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R'};
        for (int digit = 0; digit <= 9; digit++) {
            byte[] data = ("0000" + overpunch[digit]).getBytes(StandardCharsets.US_ASCII);
            BigDecimal result = ZonedDecimalCodec.decode(data, 0);
            assertEquals(new BigDecimal(-digit), result,
                    "Overpunch '" + overpunch[digit] + "' should decode to -" + digit);
        }
    }
}
