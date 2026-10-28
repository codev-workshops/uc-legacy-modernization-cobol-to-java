package com.carddemo.harness.compare;

import java.util.List;

public record ComparisonResult(
    List<FieldDiff> differences,
    List<String> skippedFields,
    List<String> missingKeys,
    List<String> extraKeys) {
  public ComparisonResult {
    differences = List.copyOf(differences);
    skippedFields = List.copyOf(skippedFields);
    missingKeys = List.copyOf(missingKeys);
    extraKeys = List.copyOf(extraKeys);
  }

  public boolean equal() {
    return differences.isEmpty() && missingKeys.isEmpty() && extraKeys.isEmpty();
  }
}
