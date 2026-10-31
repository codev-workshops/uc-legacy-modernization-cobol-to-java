package com.carddemo.batch.cbact01c;

/** COBOL {@code PIC X} move semantics: left-justified, space-padded, truncated on the right. */
final class Alphanumeric {

  private Alphanumeric() {}

  /** Returns {@code value} moved into a {@code PIC X(length)} field. */
  static String fit(String value, int length) {
    if (value.length() >= length) {
      return value.substring(0, length);
    }
    return value + " ".repeat(length - value.length());
  }
}
