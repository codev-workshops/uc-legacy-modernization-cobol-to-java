package com.carddemo.harness.codec;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Pic(String source, boolean alphanumeric, boolean signed, int intDigits, int scale) {
  private static final Pattern ALPHA = Pattern.compile("X(?:\\((\\d+)\\))?");
  private static final Pattern NUMERIC = Pattern.compile("(S)?(9+(?:\\((\\d+)\\))?)(?:V(9+(?:\\((\\d+)\\))?))?");

  public static Pic parse(String source) {
    String value = source.toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
    Matcher alpha = ALPHA.matcher(value);
    if (alpha.matches()) {
      return new Pic(value, true, false, Integer.parseInt(alpha.group(1) == null ? "1" : alpha.group(1)), 0);
    }
    Matcher numeric = NUMERIC.matcher(value);
    if (!numeric.matches()) {
      throw new IllegalArgumentException("Unsupported PIC: " + source);
    }
    return new Pic(value, false, numeric.group(1) != null,
        digits(numeric.group(2), numeric.group(3)), digits(numeric.group(4), numeric.group(5)));
  }

  private static int digits(String part, String count) {
    if (part == null) {
      return 0;
    }
    return count == null ? part.length() : Integer.parseInt(count);
  }

  public int digitCount() {
    return intDigits + scale;
  }
}
