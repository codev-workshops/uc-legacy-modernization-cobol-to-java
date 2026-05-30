package com.carddemo.account.batch;

import java.math.BigDecimal;

/**
 * Utility methods for parsing COBOL-style fixed-width fields,
 * including signed-decimal overpunch encoding.
 */
public final class FixedWidthParseUtils {

    private FixedWidthParseUtils() {
    }

    public static String substring(String line, int start, int length) {
        int end = Math.min(start + length, line.length());
        if (start >= line.length()) {
            return "";
        }
        return line.substring(start, end).trim();
    }

    public static Long parseLong(String line, int start, int length) {
        String raw = substring(line, start, length);
        if (raw.isEmpty()) {
            return null;
        }
        return Long.parseLong(raw);
    }

    public static Integer parseInt(String line, int start, int length) {
        String raw = substring(line, start, length);
        if (raw.isEmpty()) {
            return null;
        }
        return Integer.parseInt(raw);
    }

    /**
     * Parse a COBOL signed decimal field with overpunch encoding.
     * Positive overpunch: {=0, A=1, B=2, ..., I=9
     * Negative overpunch: }=0, J=1, K=2, ..., R=9
     * Regular digits are treated as positive.
     */
    public static BigDecimal parseSignedDecimal(String line, int start, int totalWidth, int decDigits) {
        int end = Math.min(start + totalWidth, line.length());
        if (start >= line.length()) {
            return BigDecimal.ZERO;
        }
        String raw = line.substring(start, end);
        if (raw.isBlank()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String digits = raw.substring(0, raw.length() - 1);
        int lastDigit;
        boolean negative = false;

        if (lastChar >= '0' && lastChar <= '9') {
            lastDigit = lastChar - '0';
        } else if (lastChar == '{') {
            lastDigit = 0;
        } else if (lastChar >= 'A' && lastChar <= 'I') {
            lastDigit = lastChar - 'A' + 1;
        } else if (lastChar == '}') {
            lastDigit = 0;
            negative = true;
        } else if (lastChar >= 'J' && lastChar <= 'R') {
            lastDigit = lastChar - 'J' + 1;
            negative = true;
        } else {
            lastDigit = 0;
        }

        String fullDigits = digits + lastDigit;
        long unscaled = Long.parseLong(fullDigits);
        if (negative) {
            unscaled = -unscaled;
        }
        return BigDecimal.valueOf(unscaled, decDigits);
    }
}
