package com.carddemo.harness.compare;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Canonical {
  private Canonical() {}

  public static String of(BigDecimal value, int scale) {
    BigDecimal scaled = value.setScale(scale, RoundingMode.UNNECESSARY);
    if (scaled.signum() == 0) {
      scaled = BigDecimal.ZERO.setScale(scale);
    }
    return scaled.toPlainString();
  }
}
