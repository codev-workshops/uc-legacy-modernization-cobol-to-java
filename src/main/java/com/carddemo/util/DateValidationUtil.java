package com.carddemo.util;

import java.time.LocalDate;

/**
 * Replicates the date validation logic from CSUTLDTC.cbl and its copybooks
 * (CSUTLDPY.cpy, CSUTLDWY.cpy). Validates dates in CCYYMMDD format with
 * the same rules used by the COBOL CardDemo system.
 */
public final class DateValidationUtil {

    /** Result codes matching COBOL severity / flag conditions. */
    public enum ValidationResult {
        VALID,
        YEAR_BLANK,
        YEAR_INVALID,
        CENTURY_INVALID,
        MONTH_BLANK,
        MONTH_INVALID,
        DAY_BLANK,
        DAY_INVALID,
        DAY_NOT_IN_MONTH,
        FUTURE_DATE,
        NON_NUMERIC
    }

    private static final int[] DAYS_IN_MONTH =
            {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private DateValidationUtil() { }

    /**
     * Full CCYYMMDD date validation (mirrors EDIT-DATE-CCYYMMDD through
     * EDIT-DATE-LE paragraphs). Validates year, month, day, and
     * day-in-month consistency including leap-year handling.
     */
    public static ValidationResult validateDateCCYYMMDD(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return ValidationResult.NON_NUMERIC;
        }

        String yearStr = dateStr.substring(0, 4);
        String monthStr = dateStr.substring(4, 6);
        String dayStr = dateStr.substring(6, 8);

        ValidationResult yearResult = validateYear(yearStr);
        if (yearResult != ValidationResult.VALID) {
            return yearResult;
        }

        ValidationResult monthResult = validateMonth(monthStr);
        if (monthResult != ValidationResult.VALID) {
            return monthResult;
        }

        int year = Integer.parseInt(yearStr);
        int month = Integer.parseInt(monthStr);

        ValidationResult dayResult = validateDay(dayStr, year, month);
        if (dayResult != ValidationResult.VALID) {
            return dayResult;
        }

        return ValidationResult.VALID;
    }

    /**
     * Year validation (mirrors EDIT-YEAR-CCYY paragraph).
     * Checks: blank, non-numeric, century must be 19 or 20.
     */
    public static ValidationResult validateYear(String yearStr) {
        if (yearStr == null || yearStr.isBlank()) {
            return ValidationResult.YEAR_BLANK;
        }
        if (!yearStr.matches("\\d{4}")) {
            return ValidationResult.YEAR_INVALID;
        }
        int century = Integer.parseInt(yearStr.substring(0, 2));
        if (century != 19 && century != 20) {
            return ValidationResult.CENTURY_INVALID;
        }
        return ValidationResult.VALID;
    }

    /**
     * Month validation (mirrors EDIT-MONTH paragraph).
     * Checks: blank, non-numeric, range 1-12.
     */
    public static ValidationResult validateMonth(String monthStr) {
        if (monthStr == null || monthStr.isBlank()) {
            return ValidationResult.MONTH_BLANK;
        }
        if (!monthStr.matches("\\d{1,2}")) {
            return ValidationResult.MONTH_INVALID;
        }
        int month = Integer.parseInt(monthStr);
        if (month < 1 || month > 12) {
            return ValidationResult.MONTH_INVALID;
        }
        return ValidationResult.VALID;
    }

    /**
     * Day validation (mirrors EDIT-DAY and EDIT-DAY-MONTH-YEAR paragraphs).
     * Checks: blank, non-numeric, range 1-31, and day-in-month consistency
     * including leap-year rules for February.
     */
    public static ValidationResult validateDay(String dayStr, int year, int month) {
        if (dayStr == null || dayStr.isBlank()) {
            return ValidationResult.DAY_BLANK;
        }
        if (!dayStr.matches("\\d{1,2}")) {
            return ValidationResult.DAY_INVALID;
        }
        int day = Integer.parseInt(dayStr);
        if (day < 1 || day > 31) {
            return ValidationResult.DAY_INVALID;
        }

        int maxDay = DAYS_IN_MONTH[month];
        if (month == 2 && isLeapYear(year)) {
            maxDay = 29;
        }
        if (day > maxDay) {
            return ValidationResult.DAY_NOT_IN_MONTH;
        }

        return ValidationResult.VALID;
    }

    /**
     * Date-of-birth validation (mirrors EDIT-DATE-OF-BIRTH paragraph).
     * First validates the date with {@link #validateDateCCYYMMDD}, then
     * checks the date is not in the future.
     */
    public static ValidationResult validateDateOfBirth(String dateStr) {
        ValidationResult result = validateDateCCYYMMDD(dateStr);
        if (result != ValidationResult.VALID) {
            return result;
        }

        int year = Integer.parseInt(dateStr.substring(0, 4));
        int month = Integer.parseInt(dateStr.substring(4, 6));
        int day = Integer.parseInt(dateStr.substring(6, 8));
        LocalDate dob = LocalDate.of(year, month, day);

        if (dob.isAfter(LocalDate.now())) {
            return ValidationResult.FUTURE_DATE;
        }

        return ValidationResult.VALID;
    }

    /**
     * Leap-year calculation matching the COBOL logic in EDIT-DAY-MONTH-YEAR:
     * divisible by 4, except century years unless divisible by 400.
     */
    public static boolean isLeapYear(int year) {
        if (year % 4 != 0) {
            return false;
        }
        if (year % 100 != 0) {
            return true;
        }
        return year % 400 == 0;
    }
}
