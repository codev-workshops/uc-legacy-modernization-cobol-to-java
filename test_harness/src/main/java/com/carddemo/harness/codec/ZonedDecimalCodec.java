package com.carddemo.harness.codec;

import java.math.BigInteger;
import java.util.Arrays;

public final class ZonedDecimalCodec {
  private static final String ASCII_POSITIVE = "{ABCDEFGHI";
  private static final String ASCII_NEGATIVE = "}JKLMNOPQR";

  public Object decode(byte[] bytes, Pic pic, SourceEncoding encoding) {
    StringBuilder digits = new StringBuilder(bytes.length);
    boolean negative;
    int last = bytes[bytes.length - 1] & 0xff;
    int lastDigit = last & 0x0f;
    if (encoding == SourceEncoding.ASCII) {
      for (int index = 0; index < bytes.length - 1; index++) {
        int value = bytes[index] & 0xff;
        if (value < '0' || value > '9') {
          return Undecodable.of("ZONED", bytes);
        }
        digits.append((char) value);
      }
      char sign = (char) last;
      if (sign >= '0' && sign <= '9') {
        lastDigit = sign - '0';
        negative = false;
      } else if (pic.signed() && ASCII_POSITIVE.indexOf(sign) >= 0) {
        lastDigit = ASCII_POSITIVE.indexOf(sign);
        negative = false;
      } else if (pic.signed() && ASCII_NEGATIVE.indexOf(sign) >= 0) {
        lastDigit = ASCII_NEGATIVE.indexOf(sign);
        negative = true;
      } else {
        return Undecodable.of("ZONED", bytes);
      }
    } else {
      if (lastDigit > 9) {
        return Undecodable.of("ZONED", bytes);
      }
      for (int index = 0; index < bytes.length - 1; index++) {
        int value = bytes[index] & 0xff;
        if ((value >>> 4) != 0xf || (value & 0x0f) > 9) {
          return Undecodable.of("ZONED", bytes);
        }
        digits.append((char) ('0' + (value & 0x0f)));
      }
      int zone = last >>> 4;
      if ((zone != 0xc && zone != 0xd && zone != 0xf) || (!pic.signed() && zone != 0xf)) {
        return Undecodable.of("ZONED", bytes);
      }
      negative = zone == 0xd;
    }
    digits.append((char) ('0' + lastDigit));
    BigInteger value = new BigInteger(digits.toString());
    if (negative && value.signum() == 0) {
      return Undecodable.of("ZONED_NEGATIVE_ZERO", bytes);
    }
    return canonical(value, negative, pic.scale());
  }

  public byte[] encode(String value, Pic pic, SourceEncoding encoding) {
    BigInteger unscaled = unscaled(value, pic);
    boolean negative = unscaled.signum() < 0;
    char[] chars = padDigits(unscaled.abs().toString(), pic.digitCount());
    byte[] output = new byte[chars.length];
    for (int index = 0; index < chars.length - 1; index++) {
      output[index] = (byte) chars[index];
    }
    int digit = chars[chars.length - 1] - '0';
    if (encoding == SourceEncoding.ASCII) {
      output[output.length - 1] = (byte) ((pic.signed()
          ? (negative ? ASCII_NEGATIVE : ASCII_POSITIVE)
          : "0123456789").charAt(digit));
    } else {
      output[output.length - 1] = (byte) (((pic.signed() ? (negative ? 0xd : 0xc) : 0xf) << 4)
          | digit);
      for (int index = 0; index < output.length - 1; index++) {
        output[index] = (byte) (0xf0 | (chars[index] - '0'));
      }
    }
    return output;
  }

  static BigInteger unscaled(String value, Pic pic) {
    java.math.BigDecimal decimal = new java.math.BigDecimal(value);
    if (decimal.scale() != pic.scale()) {
      decimal = decimal.setScale(pic.scale(), java.math.RoundingMode.UNNECESSARY);
    }
    return decimal.movePointRight(pic.scale()).toBigIntegerExact();
  }

  static String canonical(BigInteger value, boolean negative, int scale) {
    String rawDigits = value.toString();
    String digits = new String(padDigits(rawDigits, Math.max(rawDigits.length(), scale + 1)));
    String formatted = scale == 0 ? digits
        : digits.substring(0, digits.length() - scale) + "." + digits.substring(digits.length() - scale);
    return negative && value.signum() != 0 ? "-" + formatted : formatted;
  }

  static char[] padDigits(String digits, int length) {
    if (digits.length() > length) {
      throw new IllegalArgumentException("Value exceeds PIC digits: " + digits);
    }
    char[] result = new char[length];
    Arrays.fill(result, '0');
    digits.getChars(0, digits.length(), result, length - digits.length());
    return result;
  }
}
