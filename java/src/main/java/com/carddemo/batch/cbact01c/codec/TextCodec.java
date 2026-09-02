package com.carddemo.batch.cbact01c.codec;

import java.util.Arrays;

/** PIC X(n): fixed-width text, space padded on the right, truncated on the right (COBOL MOVE). */
public final class TextCodec {

    private TextCodec() {
    }

    public static byte[] encode(String text, int len, CobolCharset cs) {
        byte[] src = cs.encode(text);
        if (src.length == len) {
            return src;
        }
        byte[] out = new byte[len];
        Arrays.fill(out, cs.encode(" ")[0]);
        System.arraycopy(src, 0, out, 0, Math.min(src.length, len));
        return out;
    }

    public static String decode(byte[] src, int off, int len, CobolCharset cs) {
        return cs.decode(src, off, len);
    }

    public static String spaces(int len) {
        return " ".repeat(len);
    }
}
