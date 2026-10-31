package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Codec for signed zoned-decimal ({@code PIC S9(n)V9(m)} DISPLAY) fields in the ASCII rendition of
 * EBCDIC data: the sign is overpunched on the last digit ({@code &#123;} and {@code A}-{@code I}
 * for +0..+9, {@code &#125;} and {@code J}-{@code R} for -0..-9).
 */
public final class ZonedDecimal {

  private static final String POSITIVE = "{ABCDEFGHI";
  private static final String NEGATIVE = "}JKLMNOPQR";

  private ZonedDecimal() {}

  /**
   * Decodes a zoned-decimal field. A plain last digit (no overpunch) is read as positive.
   *
   * @param field the field text, one character per digit
   * @param scale the number of implied decimal places ({@code V99} is 2)
   * @return the decoded amount
   * @throws IllegalArgumentException if the field is not valid zoned decimal
   */
  public static SignedAmount decode(String field, int scale) {
    if (field.isEmpty()) {
      throw new IllegalArgumentException("empty zoned-decimal field");
    }
    String leading = field.substring(0, field.length() - 1);
    char last = field.charAt(field.length() - 1);
    if (!leading.chars().allMatch(c -> c >= '0' && c <= '9')) {
      throw new IllegalArgumentException("invalid zoned-decimal field '" + field + "'");
    }
    int digit;
    boolean negative;
    if (last >= '0' && last <= '9') {
      digit = last - '0';
      negative = false;
    } else if (POSITIVE.indexOf(last) >= 0) {
      digit = POSITIVE.indexOf(last);
      negative = false;
    } else if (NEGATIVE.indexOf(last) >= 0) {
      digit = NEGATIVE.indexOf(last);
      negative = true;
    } else {
      throw new IllegalArgumentException("invalid zoned-decimal sign in '" + field + "'");
    }
    BigDecimal magnitude = new BigDecimal(new BigInteger(leading + digit), scale);
    return new SignedAmount(negative ? magnitude.negate() : magnitude, negative);
  }

  /**
   * Encodes an amount the way a COBOL {@code MOVE} stores it in a signed zoned-decimal field: the
   * value is aligned on the decimal point, high-order digits that do not fit are truncated, and the
   * sign is overpunched in its preferred form.
   *
   * @param amount the amount to store
   * @param digits the total number of digits in the field ({@code S9(10)V99} is 12)
   * @param scale the number of implied decimal places
   * @return the field text, exactly {@code digits} characters long
   */
  public static String encode(SignedAmount amount, int digits, int scale) {
    String text = Decimals.unscaledDigits(amount.value(), digits, scale);
    int lastDigit = text.charAt(digits - 1) - '0';
    char sign = (amount.negative() ? NEGATIVE : POSITIVE).charAt(lastDigit);
    return text.substring(0, digits - 1) + sign;
  }
}
