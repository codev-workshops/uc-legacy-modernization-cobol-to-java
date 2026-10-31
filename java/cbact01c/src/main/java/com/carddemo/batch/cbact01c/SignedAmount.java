package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A signed COBOL numeric value together with its sign flag.
 *
 * <p>{@link BigDecimal} cannot represent negative zero, but a zoned-decimal field can (for example
 * {@code 00000000000&#125;}), and CBACT01C copies such a field byte for byte. The explicit {@code
 * negative} flag keeps that sign so the output matches the COBOL program.
 *
 * @param value the numeric value
 * @param negative whether the field carries a negative sign (true for negative zero)
 */
public record SignedAmount(BigDecimal value, boolean negative) {

  /** Positive zero, the value COBOL {@code INITIALIZE} gives a signed numeric field. */
  public static final SignedAmount ZERO = new SignedAmount(BigDecimal.ZERO, false);

  /** Validates that the sign flag agrees with the value. */
  public SignedAmount {
    Objects.requireNonNull(value, "value");
    if (value.signum() != 0 && negative != (value.signum() < 0)) {
      throw new IllegalArgumentException("sign flag disagrees with value " + value);
    }
  }

  /** Returns the amount for {@code value}, negative exactly when the value is below zero. */
  public static SignedAmount of(BigDecimal value) {
    return new SignedAmount(value, value.signum() < 0);
  }

  /** Returns the amount for a decimal literal such as {@code "-1025.00"}. */
  public static SignedAmount of(String value) {
    return of(new BigDecimal(value));
  }

  /** Returns whether the value is zero; COBOL treats negative zero as equal to zero. */
  public boolean isZero() {
    return value.signum() == 0;
  }
}
