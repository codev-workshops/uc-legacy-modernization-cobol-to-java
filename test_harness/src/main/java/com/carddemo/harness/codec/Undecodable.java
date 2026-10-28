package com.carddemo.harness.codec;

import java.util.HexFormat;
import java.util.Map;

public record Undecodable(String type, String hex) {
  public Undecodable {
    hex = hex.toUpperCase();
  }

  public static Undecodable of(String type, byte[] bytes) {
    return new Undecodable(type, HexFormat.of().withUpperCase().formatHex(bytes));
  }

  public static Undecodable fromMap(Map<String, Object> value) {
    return new Undecodable((String) value.get("$type"), (String) value.get("hex"));
  }

  public byte[] bytes() {
    return HexFormat.of().parseHex(hex);
  }
}
