package com.carddemo.harness.codec;

import java.util.List;

public record FieldDef(
    String name,
    int offset,
    int length,
    Pic pic,
    Usage usage,
    int occurs,
    List<FieldDef> children,
    String redefines,
    boolean filler) {
  public FieldDef {
    children = List.copyOf(children);
    if (offset < 0 || length < 1 || occurs < 1) {
      throw new IllegalArgumentException("Invalid field storage for " + name);
    }
  }

  public boolean isGroup() {
    return !children.isEmpty();
  }

  public FieldDef field(String childName) {
    return children.stream().filter(field -> field.name.equals(childName)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown field " + childName));
  }

  public static FieldDef elementary(String name, int offset, int length, String pic, Usage usage) {
    return new FieldDef(name, offset, length, Pic.parse(pic), usage, 1, List.of(), null,
        "FILLER".equals(name));
  }

  public static FieldDef group(String name, int offset, int length, int occurs, List<FieldDef> children) {
    return new FieldDef(name, offset, length, null, Usage.DISPLAY, occurs, children, null,
        "FILLER".equals(name));
  }
}
