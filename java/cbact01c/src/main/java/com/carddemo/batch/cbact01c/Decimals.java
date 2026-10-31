package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/** Decimal alignment shared by the zoned and packed codecs. */
final class Decimals {

  private Decimals() {}

  /**
   * Returns the absolute unscaled value of {@code value} at {@code scale} as exactly {@code digits}
   * decimal digits. Like a COBOL {@code MOVE}, extra decimal places and high-order digits that do
   * not fit are truncated.
   */
  static String unscaledDigits(BigDecimal value, int digits, int scale) {
    BigInteger unscaled = value.setScale(scale, RoundingMode.DOWN).unscaledValue().abs();
    String text = unscaled.mod(BigInteger.TEN.pow(digits)).toString();
    return "0".repeat(digits - text.length()) + text;
  }
}
