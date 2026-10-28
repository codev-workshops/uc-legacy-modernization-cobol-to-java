package com.carddemo.harness.recon;

import java.util.Map;

public record DatasetRecon(
    int recordCount,
    Map<String, String> controlTotals,
    String keyMin,
    String keyMax,
    boolean keyUnique,
    int duplicateKeyCount,
    String sourceSha256) {
  public DatasetRecon {
    controlTotals = Map.copyOf(controlTotals);
  }
}
