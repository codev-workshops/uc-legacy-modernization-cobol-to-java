package com.carddemo.batch.cbact01c;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/** Builds a fixed-layout record field by field, checking the final length against the layout. */
final class RecordBuffer {

  private final ByteArrayOutputStream bytes;
  private final int length;

  RecordBuffer(int length) {
    this.length = length;
    this.bytes = new ByteArrayOutputStream(length);
  }

  /** Appends a {@code PIC X(width)} or unsigned {@code PIC 9(width)} DISPLAY field. */
  RecordBuffer text(String value, int width) {
    bytes.writeBytes(Alphanumeric.fit(value, width).getBytes(StandardCharsets.ISO_8859_1));
    return this;
  }

  /** Appends a signed zoned-decimal DISPLAY field. */
  RecordBuffer zoned(SignedAmount value, int digits, int scale) {
    return text(ZonedDecimal.encode(value, digits, scale), digits);
  }

  /** Appends a signed {@code COMP-3} field. */
  RecordBuffer packed(BigDecimal value, int digits, int scale) {
    return raw(PackedDecimal.encode(value, digits, scale));
  }

  /** Appends bytes as they are. */
  RecordBuffer raw(byte[] value) {
    bytes.writeBytes(value);
    return this;
  }

  /** Returns the record bytes. */
  byte[] toBytes() {
    if (bytes.size() != length) {
      throw new IllegalStateException("record is " + bytes.size() + " bytes, layout is " + length);
    }
    return bytes.toByteArray();
  }
}
