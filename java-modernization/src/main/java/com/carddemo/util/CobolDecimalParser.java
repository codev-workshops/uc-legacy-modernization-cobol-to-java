package com.carddemo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Parses COBOL zoned-decimal fields (PIC S9(n)V99) from ASCII text.
 * <p>
 * In ASCII COBOL data files the sign is encoded as an overpunch on the
 * last byte:
 * <pre>
 *   Positive: { 0, A 1, B 2, C 3, D 4, E 5, F 6, G 7, H 8, I 9
 *   Negative: } 0, J 1, K 2, L 3, M 4, N 5, O 6, P 7, Q 8, R 9
 * </pre>
 */
public final class CobolDecimalParser {

    private static final String POS_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEG_OVERPUNCH = "}JKLMNOPQR";

    private CobolDecimalParser() {
    }

    /**
     * Parses a signed zoned-decimal string with an implied decimal point.
     *
     * @param raw            the raw COBOL field value (e.g. "00000001940{")
     * @param decimalPlaces  number of implied decimal digits (e.g. 2 for V99)
     * @return the numeric value as a BigDecimal
     */
    public static BigDecimal parseSignedDecimal(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO.setScale(decimalPlaces, RoundingMode.UNNECESSARY);
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String digits = raw.substring(0, raw.length() - 1);
        int lastDigit;
        boolean negative;

        int posIdx = POS_OVERPUNCH.indexOf(lastChar);
        if (posIdx >= 0) {
            lastDigit = posIdx;
            negative = false;
        } else {
            int negIdx = NEG_OVERPUNCH.indexOf(lastChar);
            if (negIdx >= 0) {
                lastDigit = negIdx;
                negative = true;
            } else if (Character.isDigit(lastChar)) {
                lastDigit = lastChar - '0';
                negative = false;
            } else {
                throw new IllegalArgumentException(
                        "Invalid overpunch character: '" + lastChar + "' in value: " + raw);
            }
        }

        String fullDigits = digits + lastDigit;
        BigDecimal value = new BigDecimal(fullDigits)
                .movePointLeft(decimalPlaces);

        return negative ? value.negate() : value;
    }

    /**
     * Formats a BigDecimal back to a COBOL zoned-decimal string with overpunch.
     *
     * @param value          the numeric value
     * @param totalDigits    total number of digits (e.g. 12 for PIC S9(10)V99)
     * @param decimalPlaces  number of implied decimal digits
     * @return the formatted COBOL field string
     */
    public static String formatSignedDecimal(BigDecimal value, int totalDigits, int decimalPlaces) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs().movePointRight(decimalPlaces);
        String digits = String.format("%0" + totalDigits + "d", abs.toBigInteger());

        char lastDigitChar = digits.charAt(digits.length() - 1);
        int lastDigitVal = lastDigitChar - '0';
        char overpunch = negative
                ? NEG_OVERPUNCH.charAt(lastDigitVal)
                : POS_OVERPUNCH.charAt(lastDigitVal);

        return digits.substring(0, digits.length() - 1) + overpunch;
    }
}
