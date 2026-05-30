package com.mainframe.carddemo.migration.codec;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZonedDecimalCodecTest {

    @Test
    void shouldDecodePositiveWithOverpunchZero() {
        // '{' = +0 → 00000001940{ = 19400 → 194.00
        assertEquals(new BigDecimal("194.00"), ZonedDecimalCodec.decode("00000001940{", 2));
    }

    @Test
    void shouldDecodePositiveWithOverpunchDigit() {
        // 'A' = +1 → 0000000100A = 1001 → 10.01
        assertEquals(new BigDecimal("10.01"), ZonedDecimalCodec.decode("0000000100A", 2));
    }

    @Test
    void shouldDecodeNegativeWithOverpunchJ() {
        // 'J' = -1 → 0000009190J = -91901 → -919.01
        assertEquals(new BigDecimal("-919.01"), ZonedDecimalCodec.decode("0000009190J", 2));
    }

    @Test
    void shouldDecodeNegativeWithOverpunchCloseBrace() {
        // '}' = -0 → 0000009190} = -91900 → -919.00
        assertEquals(new BigDecimal("-919.00"), ZonedDecimalCodec.decode("0000009190}", 2));
    }

    @Test
    void shouldDecodeUnsignedDigit() {
        assertEquals(new BigDecimal("12345"), ZonedDecimalCodec.decode("12345", 0));
    }

    @Test
    void shouldDecodeWithScale() {
        assertEquals(new BigDecimal("123.45"), ZonedDecimalCodec.decode("12345", 2));
    }

    @Test
    void shouldDecodeZeroValue() {
        assertEquals(new BigDecimal("0.00"), ZonedDecimalCodec.decode("00000000000{", 2));
    }

    @Test
    void shouldDecodeNegativeR() {
        // 'R' = -9 → 0000000010R = -109 → -1.09
        assertEquals(new BigDecimal("-1.09"), ZonedDecimalCodec.decode("0000000010R", 2));
    }

    @Test
    void shouldDecodeAllPositiveOverpunches() {
        assertEquals(new BigDecimal("0"), ZonedDecimalCodec.decode("{", 0));
        assertEquals(new BigDecimal("1"), ZonedDecimalCodec.decode("A", 0));
        assertEquals(new BigDecimal("2"), ZonedDecimalCodec.decode("B", 0));
        assertEquals(new BigDecimal("3"), ZonedDecimalCodec.decode("C", 0));
        assertEquals(new BigDecimal("4"), ZonedDecimalCodec.decode("D", 0));
        assertEquals(new BigDecimal("5"), ZonedDecimalCodec.decode("E", 0));
        assertEquals(new BigDecimal("6"), ZonedDecimalCodec.decode("F", 0));
        assertEquals(new BigDecimal("7"), ZonedDecimalCodec.decode("G", 0));
        assertEquals(new BigDecimal("8"), ZonedDecimalCodec.decode("H", 0));
        assertEquals(new BigDecimal("9"), ZonedDecimalCodec.decode("I", 0));
    }

    @Test
    void shouldDecodeAllNegativeOverpunches() {
        assertEquals(new BigDecimal("0"), ZonedDecimalCodec.decode("}", 0));
        assertEquals(new BigDecimal("-1"), ZonedDecimalCodec.decode("J", 0));
        assertEquals(new BigDecimal("-2"), ZonedDecimalCodec.decode("K", 0));
        assertEquals(new BigDecimal("-3"), ZonedDecimalCodec.decode("L", 0));
        assertEquals(new BigDecimal("-4"), ZonedDecimalCodec.decode("M", 0));
        assertEquals(new BigDecimal("-5"), ZonedDecimalCodec.decode("N", 0));
        assertEquals(new BigDecimal("-6"), ZonedDecimalCodec.decode("O", 0));
        assertEquals(new BigDecimal("-7"), ZonedDecimalCodec.decode("P", 0));
        assertEquals(new BigDecimal("-8"), ZonedDecimalCodec.decode("Q", 0));
        assertEquals(new BigDecimal("-9"), ZonedDecimalCodec.decode("R", 0));
    }

    @Test
    void shouldReturnZeroForEmpty() {
        assertEquals(BigDecimal.ZERO, ZonedDecimalCodec.decode("", 0));
        assertEquals(BigDecimal.ZERO, ZonedDecimalCodec.decode(null, 0));
    }

    @Test
    void shouldThrowForInvalidOverpunch() {
        assertThrows(IllegalArgumentException.class, () -> ZonedDecimalCodec.decode("12X", 0));
    }

    @Test
    void shouldDecodeRealAccountData() {
        // From acctdata.txt first record: ACCT-CURR-BAL = "00000001940{" → 194.00
        assertEquals(new BigDecimal("194.00"), ZonedDecimalCodec.decode("00000001940{", 2));
        // ACCT-CREDIT-LIMIT = "00000020200{" → 2020.00
        assertEquals(new BigDecimal("2020.00"), ZonedDecimalCodec.decode("00000020200{", 2));
    }

    @Test
    void shouldDecodeRealTransactionData() {
        // From dailytran.txt: TRAN-AMT "0000005047G" → G=+7 → 50477 → 504.77
        assertEquals(new BigDecimal("504.77"), ZonedDecimalCodec.decode("0000005047G", 2));
        // TRAN-AMT "0000009190}" → }=-0 → -91900 → -919.00
        assertEquals(new BigDecimal("-919.00"), ZonedDecimalCodec.decode("0000009190}", 2));
    }
}
