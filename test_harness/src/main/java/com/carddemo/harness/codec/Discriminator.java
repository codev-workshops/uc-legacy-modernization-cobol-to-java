package com.carddemo.harness.codec;

import java.util.Map;

public record Discriminator(String field, Map<String, String> branches) {
  public Discriminator {
    branches = Map.copyOf(branches);
  }
}
