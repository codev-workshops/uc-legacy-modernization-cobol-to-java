package com.carddemo.batch.cbact01c.model;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.codec.TextCodec;
import com.carddemo.batch.cbact01c.codec.ZonedDecimalCodec;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

/** VBRC-REC2, 39 bytes: id, current balance, credit limit, first 4 chars of the original reissue date. */
public record Vb2Rec(String acctId, BigDecimal currBal, BigDecimal creditLimit, String reissueYyyy) {

    public static final int LENGTH = 39;

    public static Vb2Rec fromBytes(byte[] b, CobolCharset cs) {
        if (b.length != LENGTH) {
            throw new IllegalArgumentException("VBRC-REC2 must be " + LENGTH + " bytes, got " + b.length);
        }
        return new Vb2Rec(
                ZonedDecimalCodec.decodeUnsigned(b, 0, 11, cs),
                ZonedDecimalCodec.decodeSigned(b, 11, 10, 2, cs),
                ZonedDecimalCodec.decodeSigned(b, 23, 10, 2, cs),
                TextCodec.decode(b, 35, 4, cs));
    }

    public byte[] toBytes(CobolCharset cs) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(LENGTH);
        out.writeBytes(ZonedDecimalCodec.encodeUnsigned(acctId, cs));
        out.writeBytes(ZonedDecimalCodec.encodeSigned(currBal, 10, 2, cs));
        out.writeBytes(ZonedDecimalCodec.encodeSigned(creditLimit, 10, 2, cs));
        out.writeBytes(TextCodec.encode(reissueYyyy, 4, cs));
        return out.toByteArray();
    }
}
