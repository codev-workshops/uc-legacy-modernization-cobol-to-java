package com.carddemo.harness.compare;

import com.carddemo.harness.codec.FieldDef;
import com.carddemo.harness.codec.RecordCodec;
import com.carddemo.harness.codec.RecordLayout;
import com.carddemo.harness.codec.SourceEncoding;
import java.util.ArrayList;
import java.util.List;

public final class ByteRoundTripChecker {
  public List<ByteDifference> check(byte[] raw, RecordLayout layout, SourceEncoding encoding) {
    byte[] encoded = new RecordCodec(layout).encode(new RecordCodec(layout).decode(raw, encoding), encoding);
    List<ByteDifference> differences = new ArrayList<>();
    for (int offset = 0; offset < raw.length; offset++) {
      if (raw[offset] != encoded[offset]) {
        differences.add(new ByteDifference(offset, fieldName(layout.fields(), offset),
            String.format("%02X", raw[offset] & 0xff), String.format("%02X", encoded[offset] & 0xff)));
      }
    }
    return differences;
  }

  private String fieldName(List<FieldDef> fields, int offset) {
    String match = null;
    int matchLength = Integer.MAX_VALUE;
    for (FieldDef field : fields) {
      int length = field.length() * field.occurs();
      if (offset >= field.offset() && offset < field.offset() + length) {
        if (!field.children().isEmpty()) {
          String nested = fieldName(field.children(), offset);
          if (nested != null) {
            int nestedLength = matchingLength(field.children(), offset);
            if (nestedLength < matchLength) {
              match = nested;
              matchLength = nestedLength;
            }
            continue;
          }
        }
        if (length < matchLength) {
          match = field.name();
          matchLength = length;
        }
      }
    }
    return match == null ? "UNKNOWN" : match;
  }

  private int matchingLength(List<FieldDef> fields, int offset) {
    int match = Integer.MAX_VALUE;
    for (FieldDef field : fields) {
      int length = field.length() * field.occurs();
      if (offset >= field.offset() && offset < field.offset() + length) {
        int nested = field.children().isEmpty() ? length : matchingLength(field.children(), offset);
        match = Math.min(match, nested);
      }
    }
    return match;
  }

  public record ByteDifference(int offset, String fieldName, String expectedHex, String actualHex) {}
}
