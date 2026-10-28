package com.carddemo.harness.loader;

import java.util.List;
import java.util.Map;

public record RawRecords(List<byte[]> records, Map<String, Integer> terminators, int paddedRecords,
                        byte[] originalBytes) {
  public RawRecords {
    records = records.stream().map(byte[]::clone).toList();
    terminators = Map.copyOf(terminators);
    originalBytes = originalBytes.clone();
  }
}
