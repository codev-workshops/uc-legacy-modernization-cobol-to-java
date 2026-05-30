package com.mainframe.carddemo.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Migrated from CSUTLDTC.cbl — validates date strings against a format string,
 * returning a {@link DateValidationResult} with one of the 9 CEEDAYS feedback codes.
 *
 * <p>Supported format strings (case-insensitive):
 * <ul>
 *   <li>{@code YYYY-MM-DD}  (ISO format)</li>
 *   <li>{@code MM/DD/YYYY}  (US format)</li>
 *   <li>{@code DD/MM/YYYY}  (European format)</li>
 *   <li>{@code YYYYMMDD}    (compact format)</li>
 * </ul>
 */
public class DateValidationService {

    private static final Map<String, DateTimeFormatter> FORMATTERS = new LinkedHashMap<>();

    static {
        FORMATTERS.put("YYYY-MM-DD",
                DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT));
        FORMATTERS.put("MM/DD/YYYY",
                DateTimeFormatter.ofPattern("MM/dd/uuuu").withResolverStyle(ResolverStyle.STRICT));
        FORMATTERS.put("DD/MM/YYYY",
                DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
        FORMATTERS.put("YYYYMMDD",
                DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
    }

    private DateValidationService() {
    }

    /**
     * Validates a date string against the given format string.
     *
     * @param dateStr   the date string to validate
     * @param formatStr one of the supported format strings (case-insensitive)
     * @return a {@link DateValidationResult} containing the feedback code
     */
    public static DateValidationResult validate(String dateStr, String formatStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return DateValidationResult.invalid(DateFeedbackCode.FC_INSUFFICIENT_DATA);
        }

        if (formatStr == null || formatStr.isBlank()) {
            return DateValidationResult.invalid(DateFeedbackCode.FC_BAD_PIC_STRING);
        }

        String normalizedFormat = formatStr.trim().toUpperCase();
        DateTimeFormatter formatter = FORMATTERS.get(normalizedFormat);

        if (formatter == null) {
            return DateValidationResult.invalid(DateFeedbackCode.FC_BAD_PIC_STRING);
        }

        String trimmed = dateStr.trim();

        if (trimmed.isEmpty()) {
            return DateValidationResult.invalid(DateFeedbackCode.FC_INSUFFICIENT_DATA);
        }

        if (!hasEnoughData(trimmed, normalizedFormat)) {
            return DateValidationResult.invalid(DateFeedbackCode.FC_INSUFFICIENT_DATA);
        }

        if (containsNonNumericData(trimmed, normalizedFormat)) {
            return DateValidationResult.invalid(DateFeedbackCode.FC_NON_NUMERIC_DATA);
        }

        try {
            LocalDate parsed = LocalDate.parse(trimmed, formatter);

            if (parsed.getYear() == 0) {
                return DateValidationResult.invalid(DateFeedbackCode.FC_YEAR_IN_ERA_ZERO);
            }

            if (parsed.getYear() < 1 || parsed.getYear() > 9999) {
                return DateValidationResult.invalid(DateFeedbackCode.FC_UNSUPP_RANGE);
            }

            return DateValidationResult.valid();
        } catch (DateTimeParseException e) {
            return classifyParseException(trimmed, normalizedFormat);
        }
    }

    /**
     * Convenience method: validates and returns whether the date is valid.
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return false;
        }
        return validate(dateStr, "YYYY-MM-DD").isValid();
    }

    /**
     * Convenience method: parses a date string in ISO format, or null if invalid.
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    public static boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private static boolean hasEnoughData(String dateStr, String format) {
        return switch (format) {
            case "YYYY-MM-DD" -> dateStr.length() >= 10;
            case "MM/DD/YYYY" -> dateStr.length() >= 10;
            case "DD/MM/YYYY" -> dateStr.length() >= 10;
            case "YYYYMMDD" -> dateStr.length() >= 8;
            default -> false;
        };
    }

    private static boolean containsNonNumericData(String dateStr, String format) {
        String digitsOnly = dateStr.replaceAll("[^0-9]", "");
        String separators;
        int expectedDigits;

        switch (format) {
            case "YYYY-MM-DD" -> {
                separators = "-";
                expectedDigits = 8;
            }
            case "MM/DD/YYYY", "DD/MM/YYYY" -> {
                separators = "/";
                expectedDigits = 8;
            }
            case "YYYYMMDD" -> {
                separators = "";
                expectedDigits = 8;
            }
            default -> {
                return true;
            }
        }

        String stripped = dateStr;
        for (char sep : separators.toCharArray()) {
            stripped = stripped.replace(String.valueOf(sep), "");
        }

        if (!stripped.chars().allMatch(Character::isDigit)) {
            return true;
        }

        return digitsOnly.length() < expectedDigits;
    }

    private static DateValidationResult classifyParseException(String dateStr, String format) {
        String digitsOnly = dateStr.replaceAll("[^0-9]", "");

        int monthValue;
        int dayValue;
        int yearValue;

        try {
            switch (format) {
                case "YYYY-MM-DD" -> {
                    String[] parts = dateStr.split("-");
                    if (parts.length < 3) {
                        return DateValidationResult.invalid(DateFeedbackCode.FC_BAD_DATE_VALUE);
                    }
                    yearValue = Integer.parseInt(parts[0]);
                    monthValue = Integer.parseInt(parts[1]);
                    dayValue = Integer.parseInt(parts[2]);
                }
                case "MM/DD/YYYY" -> {
                    String[] parts = dateStr.split("/");
                    if (parts.length < 3) {
                        return DateValidationResult.invalid(DateFeedbackCode.FC_BAD_DATE_VALUE);
                    }
                    monthValue = Integer.parseInt(parts[0]);
                    dayValue = Integer.parseInt(parts[1]);
                    yearValue = Integer.parseInt(parts[2]);
                }
                case "DD/MM/YYYY" -> {
                    String[] parts = dateStr.split("/");
                    if (parts.length < 3) {
                        return DateValidationResult.invalid(DateFeedbackCode.FC_BAD_DATE_VALUE);
                    }
                    dayValue = Integer.parseInt(parts[0]);
                    monthValue = Integer.parseInt(parts[1]);
                    yearValue = Integer.parseInt(parts[2]);
                }
                case "YYYYMMDD" -> {
                    if (digitsOnly.length() < 8) {
                        return DateValidationResult.invalid(DateFeedbackCode.FC_BAD_DATE_VALUE);
                    }
                    yearValue = Integer.parseInt(digitsOnly.substring(0, 4));
                    monthValue = Integer.parseInt(digitsOnly.substring(4, 6));
                    dayValue = Integer.parseInt(digitsOnly.substring(6, 8));
                }
                default -> {
                    return DateValidationResult.invalid(DateFeedbackCode.FC_BAD_DATE_VALUE);
                }
            }
        } catch (NumberFormatException e) {
            return DateValidationResult.invalid(DateFeedbackCode.FC_NON_NUMERIC_DATA);
        }

        if (yearValue == 0) {
            return DateValidationResult.invalid(DateFeedbackCode.FC_YEAR_IN_ERA_ZERO);
        }

        if (monthValue < 1 || monthValue > 12) {
            return DateValidationResult.invalid(DateFeedbackCode.FC_INVALID_MONTH);
        }

        if (yearValue < 1 || yearValue > 9999) {
            return DateValidationResult.invalid(DateFeedbackCode.FC_UNSUPP_RANGE);
        }

        return DateValidationResult.invalid(DateFeedbackCode.FC_BAD_DATE_VALUE);
    }
}
