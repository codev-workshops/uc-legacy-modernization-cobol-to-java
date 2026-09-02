package com.carddemo.batch.cbact01c.model;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.codec.TextCodec;
import com.carddemo.batch.cbact01c.codec.ZonedDecimalCodec;

/** VBRC-REC1, 12 bytes: account id + active status. */
public record Vb1Rec(String acctId, String activeStatus) {

    public static final int LENGTH = 12;

    public static Vb1Rec fromBytes(byte[] b, CobolCharset cs) {
        if (b.length != LENGTH) {
            throw new IllegalArgumentException("VBRC-REC1 must be " + LENGTH + " bytes, got " + b.length);
        }
        return new Vb1Rec(ZonedDecimalCodec.decodeUnsigned(b, 0, 11, cs), TextCodec.decode(b, 11, 1, cs));
    }

    public byte[] toBytes(CobolCharset cs) {
        byte[] out = new byte[LENGTH];
        System.arraycopy(ZonedDecimalCodec.encodeUnsigned(acctId, cs), 0, out, 0, 11);
        System.arraycopy(TextCodec.encode(activeStatus, 1, cs), 0, out, 11, 1);
        return out;
    }
}
