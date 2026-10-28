package com.carddemo.harness.codec;

import java.util.Arrays;

public final class AlphanumericCodec {
  public Object decode(byte[] bytes, Pic pic, SourceEncoding encoding) {
    if (encoding == SourceEncoding.ASCII) {
      if (Arrays.stream(toInts(bytes)).anyMatch(value -> value < 0x20 || value > 0x7e)) {
        return Undecodable.of("ALPHANUMERIC", bytes);
      }
    } else {
      String text = new String(bytes, encoding.charset());
      if (text.chars().anyMatch(Character::isISOControl)) {
        return Undecodable.of("ALPHANUMERIC", bytes);
      }
    }
    return new String(bytes, encoding.charset());
  }

  public byte[] encode(String value, Pic pic, SourceEncoding encoding) {
    byte[] bytes = value.getBytes(encoding.charset());
    if (bytes.length != pic.intDigits()) {
      throw new IllegalArgumentException("Expected " + pic.intDigits() + " bytes, got " + bytes.length);
    }
    return bytes;
  }

  private int[] toInts(byte[] bytes) {
    return java.util.stream.IntStream.range(0, bytes.length).map(index -> bytes[index] & 0xff).toArray();
  }
}
