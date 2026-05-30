package com.carddemo.common.codec;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Decodes/encodes ASCII zoned-decimal values with COBOL sign overpunch encoding.
 * Ported from test-harness.
 */
public final class ZonedDecimalCodec {

    private ZonedDecimalCodec() {}

    public static BigDecimal decode(byte[] data, int scale) {
        return decode(data, 0, data.length, scale);
    }

    public static BigDecimal decode(byte[] data, int offset, int length, int scale) {
        if (length == 0) {
            return BigDecimal.ZERO;
        }

        StringBuilder digits = new StringBuilder(length);
        boolean negative = false;

        for (int i = 0; i < length; i++) {
            char c = (char) (data[offset + i] & 0xFF);

            if (i == length - 1) {
                int[] decoded = decodeOverpunch(c);
                negative = decoded[0] < 0;
                digits.append(decoded[1]);
            } else {
                if (c >= '0' && c <= '9') {
                    digits.append(c);
                } else {
                    int[] decoded = decodeOverpunch(c);
                    digits.append(decoded[1]);
                }
            }
        }

        BigDecimal value = new BigDecimal(digits.toString());
        if (scale > 0) {
            value = value.movePointLeft(scale);
        }
        if (negative) {
            value = value.negate();
        }
        return value;
    }

    static int[] decodeOverpunch(char c) {
        return switch (c) {
            case '{' -> new int[]{1, 0};
            case 'A' -> new int[]{1, 1};
            case 'B' -> new int[]{1, 2};
            case 'C' -> new int[]{1, 3};
            case 'D' -> new int[]{1, 4};
            case 'E' -> new int[]{1, 5};
            case 'F' -> new int[]{1, 6};
            case 'G' -> new int[]{1, 7};
            case 'H' -> new int[]{1, 8};
            case 'I' -> new int[]{1, 9};
            case '}' -> new int[]{-1, 0};
            case 'J' -> new int[]{-1, 1};
            case 'K' -> new int[]{-1, 2};
            case 'L' -> new int[]{-1, 3};
            case 'M' -> new int[]{-1, 4};
            case 'N' -> new int[]{-1, 5};
            case 'O' -> new int[]{-1, 6};
            case 'P' -> new int[]{-1, 7};
            case 'Q' -> new int[]{-1, 8};
            case 'R' -> new int[]{-1, 9};
            default -> {
                if (c >= '0' && c <= '9') {
                    yield new int[]{1, c - '0'};
                }
                throw new IllegalArgumentException(
                        "Invalid overpunch character: '" + c + "' (0x" + Integer.toHexString(c) + ")");
            }
        };
    }

    public static byte[] encode(BigDecimal value, int length, int scale) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();
        if (scale > 0) {
            abs = abs.movePointRight(scale);
        }
        String digits = abs.toBigInteger().toString();

        while (digits.length() < length) {
            digits = "0" + digits;
        }
        if (digits.length() > length) {
            throw new IllegalArgumentException(
                    "Value " + value + " exceeds field length " + length);
        }

        byte[] result = digits.getBytes(StandardCharsets.US_ASCII);

        int lastDigit = result[length - 1] - '0';
        if (negative) {
            result[length - 1] = (byte) (lastDigit == 0 ? '}' : ('J' + lastDigit - 1));
        } else {
            result[length - 1] = (byte) (lastDigit == 0 ? '{' : ('A' + lastDigit - 1));
        }

        return result;
    }
}
