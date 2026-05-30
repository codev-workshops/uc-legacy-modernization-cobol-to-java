package com.carddemo.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateFormatUtilTest {

    @Test
    void formatHyphenatedToCompact() {
        String result = DateFormatUtil.formatDate("2024-01-15", "2", "1");
        assertEquals(20, result.length());
        assertEquals("20240115", result.substring(0, 8));
        assertEquals("            ", result.substring(8));
    }

    @Test
    void formatCompactToHyphenated() {
        String result = DateFormatUtil.formatDate("20240115", "1", "2");
        assertEquals(20, result.length());
        assertEquals("2024-01-15", result.substring(0, 10));
    }

    @Test
    void formatHyphenatedToHyphenated() {
        String result = DateFormatUtil.formatDate("2024-01-15", "2", "2");
        assertEquals(20, result.length());
        assertEquals("2024-01-15", result.substring(0, 10));
    }

    @Test
    void formatCompactToCompact() {
        String result = DateFormatUtil.formatDate("20240115", "1", "1");
        assertEquals(20, result.length());
        assertEquals("20240115", result.substring(0, 8));
    }

    @Test
    void outputAlways20Bytes() {
        String result = DateFormatUtil.formatDate("2024-01-15", "2", "1");
        assertEquals(20, result.length());
    }

    @Test
    void dateTruncationEdgeCase() {
        // COBDATFT outputs 20 bytes, only first 8 are YYYYMMDD.
        // Remaining bytes may be garbage (test-harness README lines 108-109).
        String result = DateFormatUtil.formatDate("2024-01-15", "2", "1");
        String yyyymmdd = result.substring(0, 8);
        assertEquals("20240115", yyyymmdd);
        // Rest should be spaces (our clean implementation)
        assertTrue(result.substring(8).isBlank());
    }

    @Test
    void nullInput() {
        String result = DateFormatUtil.formatDate(null, "1", "1");
        assertEquals(20, result.length());
        assertTrue(result.isBlank());
    }

    @Test
    void blankInput() {
        String result = DateFormatUtil.formatDate("   ", "1", "1");
        assertEquals(20, result.length());
        assertTrue(result.isBlank());
    }

    @Test
    void shortDatePadded() {
        String result = DateFormatUtil.formatDate("2024", "1", "1");
        assertEquals(20, result.length());
    }
}
