package com.carddemo.common.util;

/**
 * Replaces the COBDATFT assembler routine called at CBACT01C.cbl line 231.
 * Produces a 20-byte output where the first 8 characters are YYYYMMDD.
 * The remaining 12 bytes are space-padded.
 */
public final class DateFormatUtil {

    private static final int OUTPUT_LENGTH = 20;

    private DateFormatUtil() {}

    /**
     * Formats a date string into a 20-byte output.
     * Input can be YYYY-MM-DD (type 2) or YYYYMMDD (type 1).
     * Output first 8 chars = YYYYMMDD, rest = spaces.
     *
     * @param inputDate the date string (e.g., "2024-01-15" or "20240115")
     * @param inputType "1" for YYYYMMDD, "2" for YYYY-MM-DD
     * @param outputType "1" for YYYYMMDD, "2" for YYYY-MM-DD
     * @return 20-byte string with formatted date in first 8 (or 10) chars
     */
    public static String formatDate(String inputDate, String inputType, String outputType) {
        if (inputDate == null || inputDate.isBlank()) {
            return pad("", OUTPUT_LENGTH);
        }

        String yyyymmdd = toYyyymmdd(inputDate.trim(), inputType);

        String formatted;
        if ("2".equals(outputType)) {
            formatted = toHyphenated(yyyymmdd);
        } else {
            formatted = yyyymmdd;
        }

        return pad(formatted, OUTPUT_LENGTH);
    }

    private static String toYyyymmdd(String date, String type) {
        if ("2".equals(type)) {
            return date.replace("-", "");
        }
        if (date.length() >= 8) {
            return date.substring(0, 8);
        }
        return pad(date, 8);
    }

    private static String toHyphenated(String yyyymmdd) {
        if (yyyymmdd.length() < 8) {
            yyyymmdd = pad(yyyymmdd, 8);
        }
        return yyyymmdd.substring(0, 4) + "-"
                + yyyymmdd.substring(4, 6) + "-"
                + yyyymmdd.substring(6, 8);
    }

    private static String pad(String s, int length) {
        if (s.length() >= length) {
            return s.substring(0, length);
        }
        StringBuilder sb = new StringBuilder(length);
        sb.append(s);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
