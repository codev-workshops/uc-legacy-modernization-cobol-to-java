package com.carddemo.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateConversionUtilTest {

    @Test
    void parseCcyymmdd_validDate() {
        LocalDate result = DateConversionUtil.parseCcyymmdd("20240315");
        assertEquals(LocalDate.of(2024, 3, 15), result);
    }

    @Test
    void parseCcyymmdd_leapYear() {
        LocalDate result = DateConversionUtil.parseCcyymmdd("20240229");
        assertEquals(LocalDate.of(2024, 2, 29), result);
    }

    @Test
    void parseCcyymmdd_lastCentury() {
        LocalDate result = DateConversionUtil.parseCcyymmdd("19991231");
        assertEquals(LocalDate.of(1999, 12, 31), result);
    }

    @Test
    void parseCcyymmdd_null() {
        assertThrows(IllegalArgumentException.class, () -> DateConversionUtil.parseCcyymmdd(null));
    }

    @Test
    void parseCcyymmdd_wrongLength() {
        assertThrows(IllegalArgumentException.class, () -> DateConversionUtil.parseCcyymmdd("2024031"));
    }

    @Test
    void parseCcyymmdd_invalidDate() {
        assertThrows(IllegalArgumentException.class, () -> DateConversionUtil.parseCcyymmdd("20241301"));
    }

    @Test
    void parseCcyymmdd_nonNumeric() {
        assertThrows(IllegalArgumentException.class, () -> DateConversionUtil.parseCcyymmdd("ABCDEFGH"));
    }

    @Test
    void parseMmDdYy_validDate() {
        LocalDate result = DateConversionUtil.parseMmDdYy("03/15/24");
        assertEquals(2024, result.getYear());
        assertEquals(3, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test
    void parseMmDdYy_null() {
        assertThrows(IllegalArgumentException.class, () -> DateConversionUtil.parseMmDdYy(null));
    }

    @Test
    void parseMmDdYy_wrongLength() {
        assertThrows(IllegalArgumentException.class, () -> DateConversionUtil.parseMmDdYy("3/15/24"));
    }

    @Test
    void parseMmDdYy_invalidDate() {
        assertThrows(IllegalArgumentException.class, () -> DateConversionUtil.parseMmDdYy("13/01/24"));
    }

    @Test
    void formatCcyymmdd_validDate() {
        String result = DateConversionUtil.formatCcyymmdd(LocalDate.of(2024, 3, 15));
        assertEquals("20240315", result);
    }

    @Test
    void formatCcyymmdd_null() {
        assertThrows(IllegalArgumentException.class, () -> DateConversionUtil.formatCcyymmdd(null));
    }

    @Test
    void toDb2Timestamp_validDateTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 3, 15, 10, 30, 45, 123456000);
        String result = DateConversionUtil.toDb2Timestamp(dt);
        assertEquals("2024-03-15-10.30.45.123456", result);
    }

    @Test
    void toDb2Timestamp_midnight() {
        LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 0, 0, 0, 0);
        String result = DateConversionUtil.toDb2Timestamp(dt);
        assertEquals("2024-01-01-00.00.00.000000", result);
    }

    @Test
    void toDb2Timestamp_null() {
        assertThrows(IllegalArgumentException.class, () -> DateConversionUtil.toDb2Timestamp(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"20240315", "19991231", "20000101", "20241231"})
    void isValidCcyymmdd_validDates(String date) {
        assertTrue(DateConversionUtil.isValidCcyymmdd(date));
    }

    @ParameterizedTest
    @ValueSource(strings = {"18000101", "21000101", "ABCDEFGH", "2024130"})
    void isValidCcyymmdd_invalidDates(String date) {
        assertFalse(DateConversionUtil.isValidCcyymmdd(date));
    }

    @Test
    void isValidCcyymmdd_null() {
        assertFalse(DateConversionUtil.isValidCcyymmdd(null));
    }

    @Test
    void isValidCcyymmdd_invalidMonth() {
        assertFalse(DateConversionUtil.isValidCcyymmdd("20241301"));
    }

    @Test
    void isValidCcyymmdd_invalidDay() {
        assertFalse(DateConversionUtil.isValidCcyymmdd("20240230"));
    }

    @Test
    void isValidCcyymmdd_nonLeapYear() {
        assertFalse(DateConversionUtil.isValidCcyymmdd("20230229"));
    }
}
