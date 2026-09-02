package com.carddemo.batch.cbact01c.codec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/** PIC S9(intDigits)V9(scale) COMP-3. byteLength = (intDigits + scale + 2) / 2. */
public final class PackedDecimalCodec {

    private PackedDecimalCodec() {
    }

    public static int byteLength(int intDigits, int scale) {
        return (intDigits + scale + 2) / 2;
    }

    /** Sign nibble C for positive/zero, D for negative. Values wider than the picture are truncated on the left. */
    public static byte[] encode(BigDecimal value, int intDigits, int scale) {
        BigInteger unscaled = value.setScale(scale, RoundingMode.DOWN).unscaledValue();
        boolean negative = unscaled.signum() < 0;
        int len = byteLength(intDigits, scale);
        int nibbles = len * 2 - 1;
        String digits = unscaled.abs().toString();
        if (digits.length() > nibbles) {
            digits = digits.substring(digits.length() - nibbles);
        }
        byte[] out = new byte[len];
        int nibble = nibbles - 1;
        for (int i = digits.length() - 1; i >= 0; i--, nibble--) {
            int d = digits.charAt(i) - '0';
            int pos = nibble / 2;
            out[pos] |= (byte) ((nibble % 2 == 0) ? d << 4 : d);
        }
        out[len - 1] |= (byte) (negative ? 0x0D : 0x0C);
        return out;
    }

    /** Accepts sign nibbles C (+), D (-) and F (unsigned). */
    public static BigDecimal decode(byte[] src, int off, int intDigits, int scale) {
        int len = byteLength(intDigits, scale);
        StringBuilder digits = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            int b = src[off + i] & 0xFF;
            appendDigit(digits, b >> 4, off + i);
            if (i < len - 1) {
                appendDigit(digits, b & 0x0F, off + i);
            }
        }
        int sign = src[off + len - 1] & 0x0F;
        boolean negative;
        switch (sign) {
            case 0x0C:
            case 0x0F:
                negative = false;
                break;
            case 0x0D:
                negative = true;
                break;
            default:
                throw new IllegalArgumentException(
                        "invalid packed sign nibble " + Integer.toHexString(sign) + " at offset " + (off + len - 1));
        }
        BigDecimal magnitude = new BigDecimal(new BigInteger(digits.toString()), scale);
        return negative ? magnitude.negate() : magnitude;
    }

    private static void appendDigit(StringBuilder sb, int nibble, int offset) {
        if (nibble > 9) {
            throw new IllegalArgumentException(
                    "invalid packed digit nibble " + Integer.toHexString(nibble) + " at offset " + offset);
        }
        sb.append((char) ('0' + nibble));
    }
}
