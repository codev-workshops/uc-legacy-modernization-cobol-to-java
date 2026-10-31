package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Codec for packed-decimal ({@code COMP-3}) fields: two digits per byte with the sign in the last
 * nibble ({@code C} positive, {@code D} negative).
 */
public final class PackedDecimal {

  private static final int POSITIVE_SIGN = 0x0C;
  private static final int NEGATIVE_SIGN = 0x0D;

  private PackedDecimal() {}

  /** Returns the storage size in bytes of a {@code COMP-3} field with {@code digits} digits. */
  public static int byteLength(int digits) {
    return digits / 2 + 1;
  }

  /**
   * Encodes an amount as a signed {@code COMP-3} field. A negative zero keeps the {@code D} sign.
   *
   * @param amount the amount to store; must fit the picture
   * @param digits the number of digits declared by the PIC clause ({@code S9(10)V99} is 12)
   * @param scale the number of implied decimal places
   * @return the packed bytes
   * @throws ArithmeticException if the amount does not fit the picture
   */
  public static byte[] encode(SignedAmount amount, int digits, int scale) {
    return encode(amount.value(), amount.negative(), digits, scale);
  }

  /**
   * Encodes a value as {@code PIC S9(digits - scale)V9(scale) COMP-3}.
   *
   * @param value the value; must fit the picture
   * @param digits total digits of the picture
   * @param scale digits after the implied decimal point
   * @return the packed bytes
   * @throws ArithmeticException if the value does not fit the picture
   */
  public static byte[] encode(BigDecimal value, int digits, int scale) {
    return encode(value, value.signum() < 0, digits, scale);
  }

  private static byte[] encode(BigDecimal value, boolean negative, int digits, int scale) {
    int length = byteLength(digits);
    int nibbleDigits = length * 2 - 1;
    String text = "0".repeat(nibbleDigits - digits) + Decimals.unscaledDigits(value, digits, scale);
    int sign = negative ? NEGATIVE_SIGN : POSITIVE_SIGN;
    byte[] packed = new byte[length];
    for (int i = 0; i < length; i++) {
      int high = text.charAt(i * 2) - '0';
      int low = i * 2 + 1 < nibbleDigits ? text.charAt(i * 2 + 1) - '0' : sign;
      packed[i] = (byte) (high << 4 | low);
    }
    return packed;
  }

  /**
   * Decodes a {@code COMP-3} field.
   *
   * @param packed the packed bytes
   * @param scale the number of implied decimal places
   * @return the decoded value
   * @throws IllegalArgumentException if a digit or the sign nibble is invalid
   */
  public static BigDecimal decode(byte[] packed, int scale) {
    if (packed.length == 0) {
      throw new IllegalArgumentException("empty packed-decimal field");
    }
    StringBuilder digits = new StringBuilder(packed.length * 2);
    for (int i = 0; i < packed.length; i++) {
      appendDigit(digits, packed[i] >> 4 & 0x0F);
      if (i < packed.length - 1) {
        appendDigit(digits, packed[i] & 0x0F);
      }
    }
    int sign = packed[packed.length - 1] & 0x0F;
    if (sign < 0x0A) {
      throw new IllegalArgumentException("invalid packed-decimal sign nibble " + sign);
    }
    BigDecimal magnitude = new BigDecimal(new BigInteger(digits.toString()), scale);
    boolean negative = sign == NEGATIVE_SIGN || sign == 0x0B;
    return negative ? magnitude.negate() : magnitude;
  }

  private static void appendDigit(StringBuilder digits, int nibble) {
    if (nibble > 9) {
      throw new IllegalArgumentException("invalid packed-decimal digit nibble " + nibble);
    }
    digits.append((char) ('0' + nibble));
  }
}
