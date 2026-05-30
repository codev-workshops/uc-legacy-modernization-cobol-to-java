package com.carddemo.account.batch;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FixedWidthParseUtilsTest {

    @Test
    void substring_normalField() {
        assertEquals("Hello", FixedWidthParseUtils.substring("Hello World", 0, 5));
    }

    @Test
    void substring_withTrailingSpaces() {
        assertEquals("Hi", FixedWidthParseUtils.substring("Hi   ", 0, 5));
    }

    @Test
    void substring_beyondLength() {
        assertEquals("", FixedWidthParseUtils.substring("abc", 5, 3));
    }

    @Test
    void substring_partialOverlap() {
        assertEquals("c", FixedWidthParseUtils.substring("abc", 2, 5));
    }

    @Test
    void parseLong_validNumber() {
        assertEquals(123L, FixedWidthParseUtils.parseLong("00000000123", 0, 11));
    }

    @Test
    void parseLong_empty() {
        assertNull(FixedWidthParseUtils.parseLong("           ", 0, 11));
    }

    @Test
    void parseInt_validNumber() {
        assertEquals(42, FixedWidthParseUtils.parseInt("042", 0, 3));
    }

    @Test
    void parseInt_empty() {
        assertNull(FixedWidthParseUtils.parseInt("   ", 0, 3));
    }

    @Test
    void parseSignedDecimal_positiveWithBrace() {
        // 00000001940{ → 19400 → 194.00
        BigDecimal result = FixedWidthParseUtils.parseSignedDecimal("00000001940{", 0, 12, 2);
        assertEquals(new BigDecimal("194.00"), result);
    }

    @Test
    void parseSignedDecimal_positiveRegularDigit() {
        // 000000019405 → 19405 → 194.05
        BigDecimal result = FixedWidthParseUtils.parseSignedDecimal("000000019405", 0, 12, 2);
        assertEquals(new BigDecimal("194.05"), result);
    }

    @Test
    void parseSignedDecimal_negativeWithOverpunch() {
        // 00000001940} → -19400 → -194.00
        BigDecimal result = FixedWidthParseUtils.parseSignedDecimal("00000001940}", 0, 12, 2);
        assertEquals(new BigDecimal("-194.00"), result);
    }

    @Test
    void parseSignedDecimal_negativeOverpunchJ() {
        // 00000001940J → -19401 → -194.01
        BigDecimal result = FixedWidthParseUtils.parseSignedDecimal("00000001940J", 0, 12, 2);
        assertEquals(new BigDecimal("-194.01"), result);
    }

    @Test
    void parseSignedDecimal_negativeOverpunchR() {
        // 00000001940R → -19409 → -194.09
        BigDecimal result = FixedWidthParseUtils.parseSignedDecimal("00000001940R", 0, 12, 2);
        assertEquals(new BigDecimal("-194.09"), result);
    }

    @Test
    void parseSignedDecimal_positiveOverpunchA() {
        // 00000001940A → +19401 → 194.01
        BigDecimal result = FixedWidthParseUtils.parseSignedDecimal("00000001940A", 0, 12, 2);
        assertEquals(new BigDecimal("194.01"), result);
    }

    @Test
    void parseSignedDecimal_positiveOverpunchI() {
        // 00000001940I → +19409 → 194.09
        BigDecimal result = FixedWidthParseUtils.parseSignedDecimal("00000001940I", 0, 12, 2);
        assertEquals(new BigDecimal("194.09"), result);
    }

    @Test
    void parseSignedDecimal_zero() {
        BigDecimal result = FixedWidthParseUtils.parseSignedDecimal("000000000000", 0, 12, 2);
        assertEquals(BigDecimal.ZERO.setScale(2), result);
    }

    @Test
    void parseSignedDecimal_blank() {
        BigDecimal result = FixedWidthParseUtils.parseSignedDecimal("            ", 0, 12, 2);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void parseSignedDecimal_withOffset() {
        String line = "XXXXXXXXX00000020200{";
        BigDecimal result = FixedWidthParseUtils.parseSignedDecimal(line, 9, 12, 2);
        assertEquals(new BigDecimal("2020.00"), result);
    }
}
