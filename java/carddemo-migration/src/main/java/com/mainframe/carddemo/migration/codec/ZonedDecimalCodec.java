package com.mainframe.carddemo.migration.codec;

import java.math.BigDecimal;

/**
 * Decodes ASCII zoned-decimal values with COBOL sign overpunch encoding.
 * Positive: '{' = +0, 'A'-'I' = +1 to +9
 * Negative: '}' = -0, 'J'-'R' = -1 to -9
 * Unsigned: '0'-'9'
 */
public final class ZonedDecimalCodec {

    private ZonedDecimalCodec() {}

    public static BigDecimal decode(String data, int scale) {
        if (data == null || data.isEmpty()) {
            return BigDecimal.ZERO;
        }
        StringBuilder digits = new StringBuilder(data.length());
        boolean negative = false;

        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (i == data.length() - 1) {
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
}
