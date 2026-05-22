package com.carddemo.harness.codec;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;

/**
 * Decodes ASCII zoned-decimal values with COBOL sign overpunch encoding.
 *
 * In COBOL DISPLAY format (ASCII), the sign is encoded in the last byte's zone nibble:
 *   Positive: '{' = +0, 'A'-'I' = +1 to +9
 *   Negative: '}' = -0, 'J'-'R' = -1 to -9
 *   Unsigned: '0'-'9' (zone nibble 0x3)
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
                // Last byte carries the sign overpunch
                int[] decoded = decodeOverpunch(c);
                negative = decoded[0] < 0;
                digits.append(decoded[1]);
            } else {
                if (c >= '0' && c <= '9') {
                    digits.append(c);
                } else {
                    // Attempt overpunch in non-last position (unusual but handle gracefully)
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

    /**
     * Decodes an overpunch character.
     * @return int[2]: [0] = sign (-1 or +1), [1] = digit (0-9)
     */
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

    /**
     * Encodes a BigDecimal value into ASCII zoned decimal with sign overpunch.
     */
    public static byte[] encode(BigDecimal value, int length, int scale) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();
        if (scale > 0) {
            abs = abs.movePointRight(scale);
        }
        String digits = abs.toBigInteger().toString();

        // Pad with leading zeros
        while (digits.length() < length) {
            digits = "0" + digits;
        }
        if (digits.length() > length) {
            throw new IllegalArgumentException(
                    "Value " + value + " exceeds field length " + length);
        }

        byte[] result = digits.getBytes(StandardCharsets.US_ASCII);

        // Apply sign overpunch to last byte
        int lastDigit = result[length - 1] - '0';
        if (negative) {
            result[length - 1] = (byte) (lastDigit == 0 ? '}' : ('J' + lastDigit - 1));
        } else {
            result[length - 1] = (byte) (lastDigit == 0 ? '{' : ('A' + lastDigit - 1));
        }

        return result;
    }
}
