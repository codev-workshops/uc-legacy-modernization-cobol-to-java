package com.carddemo.batch;

import com.carddemo.batch.util.DateFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DateFormatter, which replaces the COBDATFT assembler routine.
 * Validates date format conversions match COBOL behavior.
 */
class DateFormatterTest {

    @Test
    @DisplayName("CBACT01C uses type=2 input (YYYY-MM-DD) and outputType=2 (YYYYMMDD)")
    void convertYyyyMmDdToYyyymmdd() {
        String result = DateFormatter.formatDate("2025-05-20", "2", "2");
        assertEquals("20250520", result);
    }

    @ParameterizedTest
    @DisplayName("YYYY-MM-DD → YYYYMMDD conversions (type 2→2)")
    @CsvSource({
            "2014-11-20, 20141120",
            "2024-08-11, 20240811",
            "2023-12-16, 20231216",
            "2009-06-17, 20090617"
    })
    void type2ToType2(String input, String expected) {
        assertEquals(expected, DateFormatter.formatDate(input, "2", "2"));
    }

    @ParameterizedTest
    @DisplayName("YYYYMMDD → YYYY-MM-DD conversions (type 1→1)")
    @CsvSource({
            "20250520, 2025-05-20",
            "20141120, 2014-11-20",
            "20240811, 2024-08-11"
    })
    void type1ToType1(String input, String expected) {
        assertEquals(expected, DateFormatter.formatDate(input, "1", "1"));
    }

    @ParameterizedTest
    @DisplayName("YYYYMMDD → YYYYMMDD (type 1→2)")
    @CsvSource({
            "20250520, 20250520",
            "20141120, 20141120"
    })
    void type1ToType2(String input, String expected) {
        assertEquals(expected, DateFormatter.formatDate(input, "1", "2"));
    }

    @ParameterizedTest
    @DisplayName("YYYY-MM-DD → YYYY-MM-DD (type 2→1)")
    @CsvSource({
            "2025-05-20, 2025-05-20",
            "2014-11-20, 2014-11-20"
    })
    void type2ToType1(String input, String expected) {
        assertEquals(expected, DateFormatter.formatDate(input, "2", "1"));
    }

    @Test
    @DisplayName("Empty or blank input returns empty string")
    void emptyInput() {
        assertEquals("", DateFormatter.formatDate("", "2", "2"));
        assertEquals("", DateFormatter.formatDate("   ", "2", "2"));
        assertEquals("", DateFormatter.formatDate(null, "2", "2"));
    }

    @Test
    @DisplayName("Invalid input type throws exception")
    void invalidInputType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatter.formatDate("2025-05-20", "3", "2"));
    }

    @Test
    @DisplayName("Invalid output type throws exception")
    void invalidOutputType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatter.formatDate("2025-05-20", "2", "3"));
    }
}
