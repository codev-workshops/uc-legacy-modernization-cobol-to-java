package com.carddemo.batch.cbact01c.model;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.codec.PackedDecimalCodec;
import com.carddemo.batch.cbact01c.codec.TextCodec;
import com.carddemo.batch.cbact01c.codec.ZonedDecimalCodec;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

/** OUT-ACCT-REC (OUTFILE, FB LRECL=107). {@code currCycDebit} is COMP-3. */
public record OutAcctRec(
        String acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String groupId) {

    public static final int LENGTH = 107;

    public static OutAcctRec fromBytes(byte[] b, CobolCharset cs) {
        if (b.length != LENGTH) {
            throw new IllegalArgumentException("OUT-ACCT-REC must be " + LENGTH + " bytes, got " + b.length);
        }
        return new OutAcctRec(
                ZonedDecimalCodec.decodeUnsigned(b, 0, 11, cs),
                TextCodec.decode(b, 11, 1, cs),
                ZonedDecimalCodec.decodeSigned(b, 12, 10, 2, cs),
                ZonedDecimalCodec.decodeSigned(b, 24, 10, 2, cs),
                ZonedDecimalCodec.decodeSigned(b, 36, 10, 2, cs),
                TextCodec.decode(b, 48, 10, cs),
                TextCodec.decode(b, 58, 10, cs),
                TextCodec.decode(b, 68, 10, cs),
                ZonedDecimalCodec.decodeSigned(b, 78, 10, 2, cs),
                PackedDecimalCodec.decode(b, 90, 10, 2),
                TextCodec.decode(b, 97, 10, cs));
    }

    public byte[] toBytes(CobolCharset cs) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(LENGTH);
        out.writeBytes(ZonedDecimalCodec.encodeUnsigned(acctId, cs));
        out.writeBytes(TextCodec.encode(activeStatus, 1, cs));
        out.writeBytes(ZonedDecimalCodec.encodeSigned(currBal, 10, 2, cs));
        out.writeBytes(ZonedDecimalCodec.encodeSigned(creditLimit, 10, 2, cs));
        out.writeBytes(ZonedDecimalCodec.encodeSigned(cashCreditLimit, 10, 2, cs));
        out.writeBytes(TextCodec.encode(openDate, 10, cs));
        out.writeBytes(TextCodec.encode(expirationDate, 10, cs));
        out.writeBytes(TextCodec.encode(reissueDate, 10, cs));
        out.writeBytes(ZonedDecimalCodec.encodeSigned(currCycCredit, 10, 2, cs));
        out.writeBytes(PackedDecimalCodec.encode(currCycDebit, 10, 2));
        out.writeBytes(TextCodec.encode(groupId, 10, cs));
        return out.toByteArray();
    }
}
