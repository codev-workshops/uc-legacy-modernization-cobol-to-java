package com.carddemo.common.codec;

import java.math.BigDecimal;

/**
 * Decodes/encodes COMP-3 (packed decimal) values.
 * Ported from test-harness.
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
                digits.append(highNibble);
                negative = (lowNibble == 0x0D);
            } else {
                digits.append(highNibble);
                digits.append(lowNibble);
            }
        }

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

    public static byte[] encode(BigDecimal value, int byteLen, int scale) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();
        if (scale > 0) {
            abs = abs.movePointRight(scale);
        }
        String digitStr = abs.toBigInteger().toString();

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
