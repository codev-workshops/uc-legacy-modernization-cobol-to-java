package com.carddemo.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Replaces COBOL CSUTLDTC / COBDATFT date-conversion routines.
 * Handles COBOL date formats (YYYYMMDD, MM/DD/YY) and converts to Java LocalDate.
 * Also generates DB2-format timestamps.
 */
public final class DateConversionUtil {

    private static final DateTimeFormatter CCYYMMDD = DateTimeFormatter.ofPattern("uuuuMMdd")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter MM_DD_YY = DateTimeFormatter.ofPattern("MM/dd/uu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DB2_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    private DateConversionUtil() {
    }

    /**
     * Parse a COBOL CCYYMMDD (YYYYMMDD) string into a LocalDate.
     *
     * @param ccyymmdd 8-character date string in YYYYMMDD format
     * @return parsed LocalDate
     * @throws IllegalArgumentException if the input is null, not 8 characters, or not a valid date
     */
    public static LocalDate parseCcyymmdd(String ccyymmdd) {
        if (ccyymmdd == null || ccyymmdd.trim().length() != 8) {
            throw new IllegalArgumentException("CCYYMMDD date must be exactly 8 characters: " + ccyymmdd);
        }
        String trimmed = ccyymmdd.trim();
        try {
            return LocalDate.parse(trimmed, CCYYMMDD);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid CCYYMMDD date: " + ccyymmdd, e);
        }
    }

    /**
     * Parse a COBOL MM/DD/YY string into a LocalDate.
     * Applies century windowing: years 00-49 map to 2000-2049, 50-99 map to 1950-1999.
     *
     * @param mmddyy date string in MM/DD/YY format
     * @return parsed LocalDate
     * @throws IllegalArgumentException if the input is null or not a valid date
     */
    public static LocalDate parseMmDdYy(String mmddyy) {
        if (mmddyy == null || mmddyy.trim().length() != 8) {
            throw new IllegalArgumentException("MM/DD/YY date must be exactly 8 characters (including slashes): " + mmddyy);
        }
        String trimmed = mmddyy.trim();
        try {
            return LocalDate.parse(trimmed, MM_DD_YY);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid MM/DD/YY date: " + mmddyy, e);
        }
    }

    /**
     * Format a LocalDate as CCYYMMDD (YYYYMMDD) string.
     *
     * @param date the date to format
     * @return 8-character YYYYMMDD string
     * @throws IllegalArgumentException if date is null
     */
    public static String formatCcyymmdd(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        return date.format(CCYYMMDD);
    }

    /**
     * Generate a DB2-format timestamp string from a LocalDateTime.
     * Format: YYYY-MM-DD-HH.MM.SS.FFFFFF
     *
     * @param dateTime the datetime to format
     * @return DB2 timestamp string
     * @throws IllegalArgumentException if dateTime is null
     */
    public static String toDb2Timestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime must not be null");
        }
        return dateTime.format(DB2_TIMESTAMP);
    }

    /**
     * Validate whether a CCYYMMDD string represents a valid date
     * with century in 19xx or 20xx (matching COBOL CSUTLDPY logic).
     *
     * @param ccyymmdd 8-character date string
     * @return true if valid
     */
    public static boolean isValidCcyymmdd(String ccyymmdd) {
        if (ccyymmdd == null || ccyymmdd.trim().length() != 8) {
            return false;
        }
        String trimmed = ccyymmdd.trim();
        try {
            int century = Integer.parseInt(trimmed.substring(0, 2));
            if (century != 19 && century != 20) {
                return false;
            }
            LocalDate.parse(trimmed, CCYYMMDD);
            return true;
        } catch (NumberFormatException | DateTimeParseException e) {
            return false;
        }
    }
}
