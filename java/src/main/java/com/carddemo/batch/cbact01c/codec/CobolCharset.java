package com.carddemo.batch.cbact01c.codec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Character set of a COBOL data file: mainframe EBCDIC (IBM037) or its ASCII rendering. */
public enum CobolCharset {
    EBCDIC(Charset.forName("IBM037")),
    ASCII(StandardCharsets.US_ASCII);

    private final Charset charset;

    CobolCharset(Charset charset) {
        this.charset = charset;
    }

    public Charset charset() {
        return charset;
    }

    public byte[] encode(String text) {
        return text.getBytes(charset);
    }

    public String decode(byte[] src, int off, int len) {
        return new String(src, off, len, charset);
    }
}
