package com.carddemo.harness.codec;

import java.math.BigInteger;
import java.util.Arrays;

public final class BinaryCodec {
  public Object decode(byte[] bytes, Pic pic, SourceEncoding encoding) {
    return decode(bytes, pic, false);
  }

  public Object decode(byte[] bytes, Pic pic, boolean comp5) {
    BigInteger value = new BigInteger(1, bytes);
    if (pic.signed() && (bytes[0] & 0x80) != 0) {
      value = value.subtract(BigInteger.ONE.shiftLeft(bytes.length * 8));
    }
    if (!comp5 && value.abs().compareTo(BigInteger.TEN.pow(pic.digitCount())) >= 0) {
      return Undecodable.of("BINARY", bytes);
    }
    return ZonedDecimalCodec.canonical(value.abs(), value.signum() < 0, pic.scale());
  }

  public byte[] encode(String canonical, Pic pic, SourceEncoding encoding) {
    BigInteger value = ZonedDecimalCodec.unscaled(canonical, pic);
    int size = storageSize(pic.digitCount());
    byte[] raw = value.toByteArray();
    byte[] output = new byte[size];
    Arrays.fill(output, value.signum() < 0 ? (byte) 0xff : 0);
    int copy = Math.min(raw.length, size);
    System.arraycopy(raw, raw.length - copy, output, size - copy, copy);
    if (!pic.signed() && output[0] < 0) {
      throw new IllegalArgumentException("Unsigned binary value exceeds storage");
    }
    return output;
  }

  public static int storageSize(int digits) {
    return digits <= 4 ? 2 : digits <= 9 ? 4 : 8;
  }
}
