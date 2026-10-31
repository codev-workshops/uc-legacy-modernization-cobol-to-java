package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/** Decimal alignment shared by the zoned and packed codecs. */
final class Decimals {

  private Decimals() {}

  /**
   * Returns the absolute unscaled value of {@code value} at {@code scale} as exactly {@code digits}
   * decimal digits.
   *
   * @throws ArithmeticException if {@code value} has more than {@code scale} decimal places or more
   *     than {@code digits - scale} integer digits; a COBOL {@code MOVE} would silently truncate
   *     them, which never happens in CBACT01C (every move is between identical pictures)
   */
  static String unscaledDigits(BigDecimal value, int digits, int scale) {
    BigInteger unscaled;
    try {
      unscaled = value.setScale(scale, RoundingMode.UNNECESSARY).unscaledValue().abs();
    } catch (ArithmeticException e) {
      throw new ArithmeticException(
          value.toPlainString() + " has more than " + scale + " decimal places");
    }
    if (unscaled.compareTo(BigInteger.TEN.pow(digits)) >= 0) {
      throw new ArithmeticException(
          value.toPlainString() + " does not fit in " + (digits - scale) + " integer digits");
    }
    String text = unscaled.toString();
    return "0".repeat(digits - text.length()) + text;
  }
}
