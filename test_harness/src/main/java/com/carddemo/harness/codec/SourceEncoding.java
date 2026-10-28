package com.carddemo.harness.codec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public enum SourceEncoding {
  ASCII(StandardCharsets.ISO_8859_1),
  EBCDIC_CP037(Charset.forName("IBM037"));

  private final Charset charset;

  SourceEncoding(Charset charset) {
    this.charset = charset;
  }

  public Charset charset() {
    return charset;
  }
}
