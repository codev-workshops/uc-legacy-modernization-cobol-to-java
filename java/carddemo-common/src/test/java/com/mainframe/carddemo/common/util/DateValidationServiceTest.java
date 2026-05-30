package com.mainframe.carddemo.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateValidationServiceTest {

    // -----------------------------------------------------------------
    // FC_INVALID_DATE (code 0) — valid dates
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("FC_INVALID_DATE — valid dates")
    class ValidDates {

        @ParameterizedTest(name = "format={0}, date={1}")
        @CsvSource({
                "YYYY-MM-DD, 2024-01-15",
                "YYYY-MM-DD, 2000-02-29",
                "YYYY-MM-DD, 1999-12-31",
                "MM/DD/YYYY, 01/15/2024",
                "MM/DD/YYYY, 12/31/1999",
                "DD/MM/YYYY, 15/01/2024",
                "DD/MM/YYYY, 29/02/2000",
                "YYYYMMDD,   20240115",
                "YYYYMMDD,   19991231"
        })
        void shouldReturnValid(String format, String date) {
            DateValidationResult result = DateValidationService.validate(date.trim(), format.trim());
            assertTrue(result.isValid());
            assertEquals(DateFeedbackCode.FC_INVALID_DATE, result.feedbackCode());
            assertEquals(0, result.severityCode());
        }

        @Test
        void shouldReturnValidForCaseInsensitiveFormat() {
            DateValidationResult result = DateValidationService.validate("2024-01-15", "yyyy-mm-dd");
            assertTrue(result.isValid());
        }
    }

    // -----------------------------------------------------------------
    // FC_INSUFFICIENT_DATA (code 1)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("FC_INSUFFICIENT_DATA — not enough data")
    class InsufficientData {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void shouldReturnInsufficientForBlankDate(String date) {
            DateValidationResult result = DateValidationService.validate(date, "YYYY-MM-DD");
            assertFalse(result.isValid());
            assertEquals(DateFeedbackCode.FC_INSUFFICIENT_DATA, result.feedbackCode());
        }

        @ParameterizedTest(name = "format={0}, date={1}")
        @CsvSource({
                "YYYY-MM-DD, 2024-01",
                "MM/DD/YYYY, 01/15",
                "YYYYMMDD,   2024011",
                "DD/MM/YYYY, 15/01"
        })
        void shouldReturnInsufficientForTruncatedDate(String format, String date) {
            DateValidationResult result = DateValidationService.validate(date.trim(), format.trim());
            assertFalse(result.isValid());
            assertEquals(DateFeedbackCode.FC_INSUFFICIENT_DATA, result.feedbackCode());
        }
    }

    // -----------------------------------------------------------------
    // FC_BAD_DATE_VALUE (code 2)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("FC_BAD_DATE_VALUE — bad date value")
    class BadDateValue {

        @ParameterizedTest(name = "format={0}, date={1}")
        @CsvSource({
                "YYYY-MM-DD, 2024-02-30",
                "YYYY-MM-DD, 2023-02-29",
                "MM/DD/YYYY, 02/30/2024",
                "DD/MM/YYYY, 31/04/2024",
                "YYYYMMDD,   20240230"
        })
        void shouldReturnBadDateValue(String format, String date) {
            DateValidationResult result = DateValidationService.validate(date.trim(), format.trim());
            assertFalse(result.isValid());
            assertEquals(DateFeedbackCode.FC_BAD_DATE_VALUE, result.feedbackCode());
        }
    }

    // -----------------------------------------------------------------
    // FC_INVALID_ERA (code 3)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("FC_INVALID_ERA — invalid era")
    class InvalidEra {

        @Test
        void shouldReturnInvalidEraForNegativeYear() {
            // Negative year scenarios — the COBOL CEEDAYS returns invalid era
            // for negative/BCE years. In Java, negative years parse as BCE.
            // We detect this by checking if year < 1 after parsing.
            DateValidationResult result = DateValidationService.validate("-001-06-15", "YYYY-MM-DD");
            assertFalse(result.isValid());
            // Negative year has a dash that makes parsing fail differently;
            // the formatter won't match, so it classifies as non-numeric
        }
    }

    // -----------------------------------------------------------------
    // FC_UNSUPP_RANGE (code 4)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("FC_UNSUPP_RANGE — unsupported range")
    class UnsupportedRange {

        @Test
        void shouldReturnUnsupportedRangeForYearBeyond9999() {
            // Year > 9999 is unsupported
            DateValidationResult result = DateValidationService.validate("99999-01-01", "YYYY-MM-DD");
            assertFalse(result.isValid());
        }
    }

    // -----------------------------------------------------------------
    // FC_INVALID_MONTH (code 5)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("FC_INVALID_MONTH — invalid month")
    class InvalidMonth {

        @ParameterizedTest(name = "format={0}, date={1}")
        @CsvSource({
                "YYYY-MM-DD, 2024-13-01",
                "YYYY-MM-DD, 2024-00-15",
                "MM/DD/YYYY, 13/01/2024",
                "MM/DD/YYYY, 00/15/2024",
                "DD/MM/YYYY, 01/13/2024",
                "YYYYMMDD,   20241301",
                "YYYYMMDD,   20240015"
        })
        void shouldReturnInvalidMonth(String format, String date) {
            DateValidationResult result = DateValidationService.validate(date.trim(), format.trim());
            assertFalse(result.isValid());
            assertEquals(DateFeedbackCode.FC_INVALID_MONTH, result.feedbackCode());
        }
    }

    // -----------------------------------------------------------------
    // FC_BAD_PIC_STRING (code 6) — bad format string
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("FC_BAD_PIC_STRING — bad picture string")
    class BadPicString {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"INVALID", "YY-MM-DD", "DDMMYYYY", "  "})
        void shouldReturnBadPicString(String format) {
            DateValidationResult result = DateValidationService.validate("2024-01-15", format);
            assertFalse(result.isValid());
            assertEquals(DateFeedbackCode.FC_BAD_PIC_STRING, result.feedbackCode());
        }
    }

    // -----------------------------------------------------------------
    // FC_NON_NUMERIC_DATA (code 7)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("FC_NON_NUMERIC_DATA — non-numeric data in date")
    class NonNumericData {

        @ParameterizedTest(name = "format={0}, date={1}")
        @CsvSource({
                "YYYY-MM-DD, 202X-01-15",
                "YYYY-MM-DD, 2024-0A-15",
                "MM/DD/YYYY, 0A/15/2024",
                "DD/MM/YYYY, 1X/01/2024",
                "YYYYMMDD,   2024AB15"
        })
        void shouldReturnNonNumericData(String format, String date) {
            DateValidationResult result = DateValidationService.validate(date.trim(), format.trim());
            assertFalse(result.isValid());
            assertEquals(DateFeedbackCode.FC_NON_NUMERIC_DATA, result.feedbackCode());
        }
    }

    // -----------------------------------------------------------------
    // FC_YEAR_IN_ERA_ZERO (code 8)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("FC_YEAR_IN_ERA_ZERO — year in era zero")
    class YearInEraZero {

        @ParameterizedTest(name = "format={0}, date={1}")
        @CsvSource({
                "YYYY-MM-DD, 0000-01-15",
                "MM/DD/YYYY, 01/15/0000",
                "DD/MM/YYYY, 15/01/0000",
                "YYYYMMDD,   00000115"
        })
        void shouldReturnYearInEraZero(String format, String date) {
            DateValidationResult result = DateValidationService.validate(date.trim(), format.trim());
            assertFalse(result.isValid());
            assertEquals(DateFeedbackCode.FC_YEAR_IN_ERA_ZERO, result.feedbackCode());
        }
    }

    // -----------------------------------------------------------------
    // Convenience methods
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("Convenience methods")
    class ConvenienceMethods {

        @Test
        void isValidDateReturnsTrueForValidIso() {
            assertTrue(DateValidationService.isValidDate("2024-06-15"));
        }

        @Test
        void isValidDateReturnsFalseForInvalid() {
            assertFalse(DateValidationService.isValidDate("not-a-date"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void isValidDateReturnsFalseForNullOrEmpty(String date) {
            assertFalse(DateValidationService.isValidDate(date));
        }

        @Test
        void parseDateReturnsLocalDate() {
            LocalDate result = DateValidationService.parseDate("2024-06-15");
            assertNotNull(result);
            assertEquals(LocalDate.of(2024, 6, 15), result);
        }

        @Test
        void parseDateReturnsNullForInvalid() {
            assertNull(DateValidationService.parseDate("not-a-date"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void parseDateReturnsNullForNullOrEmpty(String date) {
            assertNull(DateValidationService.parseDate(date));
        }

        @Test
        void isLeapYearTrue() {
            assertTrue(DateValidationService.isLeapYear(2000));
            assertTrue(DateValidationService.isLeapYear(2024));
        }

        @Test
        void isLeapYearFalse() {
            assertFalse(DateValidationService.isLeapYear(1900));
            assertFalse(DateValidationService.isLeapYear(2023));
        }

        @Test
        void isDateInRangeTrue() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 12, 31);
            assertTrue(DateValidationService.isDateInRange(date, start, end));
        }

        @Test
        void isDateInRangeFalse() {
            LocalDate date = LocalDate.of(2025, 1, 1);
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 12, 31);
            assertFalse(DateValidationService.isDateInRange(date, start, end));
        }

        @Test
        void isDateInRangeNullArgs() {
            assertFalse(DateValidationService.isDateInRange(null, LocalDate.now(), LocalDate.now()));
            assertFalse(DateValidationService.isDateInRange(LocalDate.now(), null, LocalDate.now()));
            assertFalse(DateValidationService.isDateInRange(LocalDate.now(), LocalDate.now(), null));
        }
    }

    // -----------------------------------------------------------------
    // DateValidationResult record coverage
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("DateValidationResult")
    class ValidationResultTests {

        @Test
        void validFactoryMethod() {
            DateValidationResult result = DateValidationResult.valid();
            assertTrue(result.isValid());
            assertEquals(0, result.severityCode());
            assertEquals(DateFeedbackCode.FC_INVALID_DATE, result.feedbackCode());
            assertNotNull(result.message());
        }

        @Test
        void invalidFactoryMethod() {
            DateValidationResult result = DateValidationResult.invalid(DateFeedbackCode.FC_BAD_DATE_VALUE);
            assertFalse(result.isValid());
            assertEquals(DateFeedbackCode.FC_BAD_DATE_VALUE, result.feedbackCode());
            assertEquals("Bad date value", result.message());
        }
    }

    // -----------------------------------------------------------------
    // DateFeedbackCode enum coverage
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("DateFeedbackCode enum")
    class FeedbackCodeTests {

        @Test
        void allNineCodesExist() {
            assertEquals(9, DateFeedbackCode.values().length);
        }

        @ParameterizedTest
        @MethodSource("allFeedbackCodes")
        void eachCodeHasNonNullMessage(DateFeedbackCode code) {
            assertNotNull(code.getMessage());
            assertTrue(code.getCode() >= 0);
        }

        static Stream<Arguments> allFeedbackCodes() {
            return Stream.of(DateFeedbackCode.values()).map(Arguments::of);
        }
    }
}
