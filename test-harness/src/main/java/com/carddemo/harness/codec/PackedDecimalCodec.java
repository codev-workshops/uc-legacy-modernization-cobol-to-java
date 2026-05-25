package com.carddemo.harness.codec;

import java.math.BigDecimal;

/**
 * Decodes and encodes COMP-3 (packed decimal) values.
 *
 * Each byte contains two BCD digits (high nibble, low nibble).
 * The last nibble is the sign: 0x0C = positive, 0x0D = negative, 0x0F = unsigned.
 */
public final class PackedDecimalCodec {

    private PackedDecimalCodec() {}

    public static BigDecimal decode(byte[] data, int scale) {
        return decode(data, 0, data.length, scale);
    }

    public static BigDecimal decode(byte[] data, int offset, int length, int scale) {
        if (length == 0) {
            return BigDecimal.ZERO;
        }

        StringBuilder digits = new StringBuilder(length * 2);
        boolean negative = false;

        for (int i = 0; i < length; i++) {
            int b = data[offset + i] & 0xFF;
            int highNibble = (b >> 4) & 0x0F;
            int lowNibble = b & 0x0F;

            if (i == length - 1) {
                // Last byte: high nibble is data digit, low nibble is sign
                digits.append(highNibble);
                negative = (lowNibble == 0x0D);
            } else {
                digits.append(highNibble);
                digits.append(lowNibble);
            }
        }

        // Remove leading zeros but keep at least one digit
        String digitStr = digits.toString().replaceFirst("^0+(?=\\d)", "");
        if (digitStr.isEmpty()) {
            digitStr = "0";
        }

        BigDecimal value = new BigDecimal(digitStr);
        if (scale > 0) {
            value = value.movePointLeft(scale);
        }
        if (negative) {
            value = value.negate();
        }
        return value;
    }

    /**
     * Encodes a BigDecimal value into COMP-3 packed decimal format.
     *
     * @param value     the value to encode
     * @param byteLen   the target byte length
     * @param scale     implied decimal places
     * @return packed byte array
     */
    public static byte[] encode(BigDecimal value, int byteLen, int scale) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();
        if (scale > 0) {
            abs = abs.movePointRight(scale);
        }
        String digitStr = abs.toBigInteger().toString();

        // Total digit capacity: byteLen * 2 - 1 (last nibble is sign)
        int capacity = byteLen * 2 - 1;
        while (digitStr.length() < capacity) {
            digitStr = "0" + digitStr;
        }
        if (digitStr.length() > capacity) {
            throw new IllegalArgumentException(
                    "Value " + value + " exceeds packed decimal capacity of " + byteLen + " bytes");
        }

        byte[] result = new byte[byteLen];
        int digitIdx = 0;

        for (int i = 0; i < byteLen; i++) {
            int highNibble;
            int lowNibble;

            if (i == byteLen - 1) {
                // Last byte: one digit + sign nibble
                highNibble = digitStr.charAt(digitIdx) - '0';
                lowNibble = negative ? 0x0D : 0x0C;
            } else {
                highNibble = digitStr.charAt(digitIdx) - '0';
                lowNibble = digitStr.charAt(digitIdx + 1) - '0';
                digitIdx += 2;
            }

            result[i] = (byte) ((highNibble << 4) | lowNibble);
        }

        return result;
    }
}
