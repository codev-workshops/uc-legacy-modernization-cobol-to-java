package com.carddemo.batch.cbact01c.codec;

import java.math.BigDecimal;
import java.math.BigInteger;

/** PIC 9(n) and PIC S9(n)V9(m) USAGE DISPLAY (zoned decimal). */
public final class ZonedDecimalCodec {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private ZonedDecimalCodec() {
    }

    /** Unsigned PIC 9(n): digits only, zone F. Values wider than {@code digits} are truncated on the left. */
    public static byte[] encodeUnsigned(long value, int digits, CobolCharset cs) {
        if (value < 0) {
            throw new IllegalArgumentException("unsigned field cannot hold " + value);
        }
        return encodeUnsigned(leftPadOrTruncate(Long.toString(value), digits), cs);
    }

    /** Pass-through of a digit string already sized to the picture (e.g. an 11-char account id). */
    public static byte[] encodeUnsigned(String digits, CobolCharset cs) {
        for (int i = 0; i < digits.length(); i++) {
            char c = digits.charAt(i);
            if (c < '0' || c > '9') {
                throw new IllegalArgumentException("not a digit string: " + digits);
            }
        }
        return cs.encode(digits);
    }

    /** Signed PIC S9(intDigits)V9(scale), sign overpunched in the last byte (C=+ or zero, D=-). */
    public static byte[] encodeSigned(BigDecimal value, int intDigits, int scale, CobolCharset cs) {
        BigInteger unscaled = value.setScale(scale, java.math.RoundingMode.DOWN).unscaledValue();
        boolean negative = unscaled.signum() < 0;
        String digits = leftPadOrTruncate(unscaled.abs().toString(), intDigits + scale);
        int last = digits.charAt(digits.length() - 1) - '0';
        char overpunch = (negative ? NEGATIVE_OVERPUNCH : POSITIVE_OVERPUNCH).charAt(last);
        return cs.encode(digits.substring(0, digits.length() - 1) + overpunch);
    }

    public static BigDecimal decodeSigned(byte[] src, int off, int intDigits, int scale, CobolCharset cs) {
        int len = intDigits + scale;
        String text = cs.decode(src, off, len);
        char last = text.charAt(len - 1);
        boolean negative;
        int lastDigit;
        int idx;
        if ((idx = POSITIVE_OVERPUNCH.indexOf(last)) >= 0) {
            negative = false;
            lastDigit = idx;
        } else if ((idx = NEGATIVE_OVERPUNCH.indexOf(last)) >= 0) {
            negative = true;
            lastDigit = idx;
        } else if (last >= '0' && last <= '9') {
            negative = false;
            lastDigit = last - '0';
        } else {
            throw new IllegalArgumentException("invalid zoned sign byte '" + last + "' at offset " + (off + len - 1));
        }
        String digits = text.substring(0, len - 1) + (char) ('0' + lastDigit);
        checkDigits(digits, off);
        BigDecimal magnitude = new BigDecimal(new BigInteger(digits), scale);
        return negative ? magnitude.negate() : magnitude;
    }

    public static String decodeUnsigned(byte[] src, int off, int len, CobolCharset cs) {
        String digits = cs.decode(src, off, len);
        checkDigits(digits, off);
        return digits;
    }

    private static void checkDigits(String digits, int off) {
        for (int i = 0; i < digits.length(); i++) {
            char c = digits.charAt(i);
            if (c < '0' || c > '9') {
                throw new IllegalArgumentException("invalid zoned digit '" + c + "' at offset " + (off + i));
            }
        }
    }

    private static String leftPadOrTruncate(String digits, int width) {
        if (digits.length() > width) {
            return digits.substring(digits.length() - width);
        }
        StringBuilder sb = new StringBuilder(width);
        for (int i = digits.length(); i < width; i++) {
            sb.append('0');
        }
        return sb.append(digits).toString();
    }
}
