package com.carddemo.harness.codec;

import java.math.BigInteger;

public final class PackedDecimalCodec {
  public Object decode(byte[] bytes, Pic pic, SourceEncoding encoding) {
    StringBuilder digits = new StringBuilder();
    for (int index = 0; index < bytes.length; index++) {
      int high = (bytes[index] >>> 4) & 0xf;
      int low = bytes[index] & 0xf;
      if (index == bytes.length - 1) {
        if (high > 9 || (low != 0xc && low != 0xd && low != 0xf)
            || (!pic.signed() && low != 0xf)) {
          return Undecodable.of("PACKED", bytes);
        }
        digits.append(high);
        if (digits.length() == pic.digitCount() + 1 && digits.charAt(0) == '0') {
          digits.deleteCharAt(0);
        }
        if (digits.length() != pic.digitCount()) {
          return Undecodable.of("PACKED", bytes);
        }
        BigInteger value = new BigInteger(digits.toString());
        if (low == 0xd && value.signum() == 0) {
          return Undecodable.of("PACKED_NEGATIVE_ZERO", bytes);
        }
        return ZonedDecimalCodec.canonical(value, low == 0xd, pic.scale());
      }
      if (high > 9 || low > 9) {
        return Undecodable.of("PACKED", bytes);
      }
      digits.append(high).append(low);
    }
    throw new IllegalArgumentException("Packed field cannot be empty");
  }

  public byte[] encode(String value, Pic pic, SourceEncoding encoding) {
    BigInteger number = ZonedDecimalCodec.unscaled(value, pic);
    char[] digits = ZonedDecimalCodec.padDigits(number.abs().toString(), pic.digitCount());
    int nibbleCount = digits.length + 1;
    if ((nibbleCount & 1) != 0) {
      nibbleCount++;
    }
    int[] nibbles = new int[nibbleCount];
    int digitStart = nibbleCount - digits.length - 1;
    for (int i = 0; i < digits.length; i++) {
      nibbles[digitStart + i] = digits[i] - '0';
    }
    nibbles[nibbleCount - 1] = pic.signed() ? (number.signum() < 0 ? 0xd : 0xc) : 0xf;
    byte[] output = new byte[nibbles.length / 2];
    for (int i = 0; i < output.length; i++) {
      output[i] = (byte) ((nibbles[2 * i] << 4) | nibbles[2 * i + 1]);
    }
    return output;
  }
}
