package com.carddemo.util;

import static com.carddemo.util.DateValidationUtil.ValidationResult.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DateValidationUtilTest {

    private static final DateTimeFormatter CCYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ------------------------------------------------------------------
    // validateDateCCYYMMDD
    // ------------------------------------------------------------------
    @Nested
    class ValidateDateCCYYMMDD {

        @Test
        void validOrdinaryDate() {
            assertEquals(VALID, DateValidationUtil.validateDateCCYYMMDD("20240115"));
        }

        @Test
        void validLeapYearFeb29() {
            assertEquals(VALID, DateValidationUtil.validateDateCCYYMMDD("20240229"));
        }

        @Test
        void validBoundaryJan1() {
            assertEquals(VALID, DateValidationUtil.validateDateCCYYMMDD("20240101"));
        }

        @Test
        void validBoundaryDec31() {
            assertEquals(VALID, DateValidationUtil.validateDateCCYYMMDD("20241231"));
        }

        @Test
        void nullInput() {
            assertEquals(NON_NUMERIC, DateValidationUtil.validateDateCCYYMMDD(null));
        }

        @Test
        void tooShort() {
            assertEquals(NON_NUMERIC, DateValidationUtil.validateDateCCYYMMDD("202401"));
        }

        @Test
        void tooLong() {
            assertEquals(NON_NUMERIC, DateValidationUtil.validateDateCCYYMMDD("202401151"));
        }
    }

    // ------------------------------------------------------------------
    // validateYear
    // ------------------------------------------------------------------
    @Nested
    class ValidateYear {

        @Test
        void validYear20thCentury() {
            assertEquals(VALID, DateValidationUtil.validateYear("1999"));
        }

        @Test
        void validYear21stCentury() {
            assertEquals(VALID, DateValidationUtil.validateYear("2024"));
        }

        @Test
        void blankYear() {
            assertEquals(YEAR_BLANK, DateValidationUtil.validateYear("    "));
        }

        @Test
        void nullYear() {
            assertEquals(YEAR_BLANK, DateValidationUtil.validateYear(null));
        }

        @Test
        void emptyYear() {
            assertEquals(YEAR_BLANK, DateValidationUtil.validateYear(""));
        }

        @Test
        void nonNumericYear() {
            assertEquals(YEAR_INVALID, DateValidationUtil.validateYear("20AB"));
        }

        @Test
        void centuryNot19Or20() {
            assertEquals(CENTURY_INVALID, DateValidationUtil.validateYear("2100"));
        }

        @Test
        void century18Invalid() {
            assertEquals(CENTURY_INVALID, DateValidationUtil.validateYear("1899"));
        }
    }

    // ------------------------------------------------------------------
    // validateMonth
    // ------------------------------------------------------------------
    @Nested
    class ValidateMonth {

        @Test
        void validMonth01() {
            assertEquals(VALID, DateValidationUtil.validateMonth("01"));
        }

        @Test
        void validMonth12() {
            assertEquals(VALID, DateValidationUtil.validateMonth("12"));
        }

        @Test
        void blankMonth() {
            assertEquals(MONTH_BLANK, DateValidationUtil.validateMonth("  "));
        }

        @Test
        void nullMonth() {
            assertEquals(MONTH_BLANK, DateValidationUtil.validateMonth(null));
        }

        @Test
        void month00Invalid() {
            assertEquals(MONTH_INVALID, DateValidationUtil.validateMonth("00"));
        }

        @Test
        void month13Invalid() {
            assertEquals(MONTH_INVALID, DateValidationUtil.validateMonth("13"));
        }

        @Test
        void nonNumericMonth() {
            assertEquals(MONTH_INVALID, DateValidationUtil.validateMonth("AB"));
        }
    }

    // ------------------------------------------------------------------
    // validateDay
    // ------------------------------------------------------------------
    @Nested
    class ValidateDay {

        @Test
        void validDay() {
            assertEquals(VALID, DateValidationUtil.validateDay("15", 2024, 1));
        }

        @Test
        void blankDay() {
            assertEquals(DAY_BLANK, DateValidationUtil.validateDay("  ", 2024, 1));
        }

        @Test
        void nullDay() {
            assertEquals(DAY_BLANK, DateValidationUtil.validateDay(null, 2024, 1));
        }

        @Test
        void day00Invalid() {
            assertEquals(DAY_INVALID, DateValidationUtil.validateDay("00", 2024, 1));
        }

        @Test
        void day32Invalid() {
            assertEquals(DAY_INVALID, DateValidationUtil.validateDay("32", 2024, 1));
        }

        @Test
        void nonNumericDay() {
            assertEquals(DAY_INVALID, DateValidationUtil.validateDay("AB", 2024, 1));
        }

        @Test
        void feb30Invalid() {
            assertEquals(DAY_NOT_IN_MONTH, DateValidationUtil.validateDay("30", 2024, 2));
        }

        @Test
        void feb29NonLeapYear() {
            assertEquals(DAY_NOT_IN_MONTH, DateValidationUtil.validateDay("29", 2023, 2));
        }

        @Test
        void feb29LeapYear() {
            assertEquals(VALID, DateValidationUtil.validateDay("29", 2024, 2));
        }

        @Test
        void day31In30DayMonth() {
            assertEquals(DAY_NOT_IN_MONTH, DateValidationUtil.validateDay("31", 2024, 4));
        }

        @Test
        void day31In31DayMonth() {
            assertEquals(VALID, DateValidationUtil.validateDay("31", 2024, 1));
        }
    }

    // ------------------------------------------------------------------
    // isLeapYear
    // ------------------------------------------------------------------
    @Nested
    class IsLeapYear {

        @Test
        void year2024IsLeap() {
            assertTrue(DateValidationUtil.isLeapYear(2024));
        }

        @Test
        void year2023IsNotLeap() {
            assertFalse(DateValidationUtil.isLeapYear(2023));
        }

        @Test
        void year2000IsLeap() {
            assertTrue(DateValidationUtil.isLeapYear(2000));
        }

        @Test
        void year1900IsNotLeap() {
            assertFalse(DateValidationUtil.isLeapYear(1900));
        }

        @Test
        void year1600IsLeap() {
            assertTrue(DateValidationUtil.isLeapYear(1600));
        }
    }

    // ------------------------------------------------------------------
    // validateDateOfBirth
    // ------------------------------------------------------------------
    @Nested
    class ValidateDateOfBirth {

        @Test
        void validPastDate() {
            assertEquals(VALID, DateValidationUtil.validateDateOfBirth("19900101"));
        }

        @Test
        void todayIsValid() {
            String today = LocalDate.now().format(CCYYMMDD);
            assertEquals(VALID, DateValidationUtil.validateDateOfBirth(today));
        }

        @Test
        void futureDateRejected() {
            String future = LocalDate.now().plusDays(1).format(CCYYMMDD);
            assertEquals(FUTURE_DATE, DateValidationUtil.validateDateOfBirth(future));
        }

        @Test
        void invalidDatePropagates() {
            assertEquals(MONTH_INVALID, DateValidationUtil.validateDateOfBirth("20241301"));
        }

        @Test
        void feb29LeapYearPastIsValid() {
            assertEquals(VALID, DateValidationUtil.validateDateOfBirth("20000229"));
        }

        @Test
        void feb29NonLeapYearInvalid() {
            assertEquals(DAY_NOT_IN_MONTH, DateValidationUtil.validateDateOfBirth("19000229"));
        }
    }
}
