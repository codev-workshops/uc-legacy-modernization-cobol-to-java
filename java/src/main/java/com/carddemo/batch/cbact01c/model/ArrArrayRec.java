package com.carddemo.batch.cbact01c.model;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.codec.PackedDecimalCodec;
import com.carddemo.batch.cbact01c.codec.TextCodec;
import com.carddemo.batch.cbact01c.codec.ZonedDecimalCodec;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;

/** ARR-ARRAY-REC (ARRYFILE, FB LRECL=110): id + ARR-ACCT-BAL OCCURS 5 + 4-byte filler. */
public record ArrArrayRec(String acctId, ArrAcctBal[] bal, String filler) {

    public static final int LENGTH = 110;
    public static final int OCCURS = 5;
    public static final BigDecimal ZERO = new BigDecimal("0.00");

    /** One ARR-ACCT-BAL occurrence (19 bytes): zoned S9(10)V99 + COMP-3 S9(10)V99. */
    public record ArrAcctBal(BigDecimal currBal, BigDecimal currCycDebit) {

        public static final int LENGTH = 19;

        public static ArrAcctBal initialized() {
            return new ArrAcctBal(ZERO, ZERO);
        }

        public static ArrAcctBal fromBytes(byte[] b, int off, CobolCharset cs) {
            return new ArrAcctBal(
                    ZonedDecimalCodec.decodeSigned(b, off, 10, 2, cs),
                    PackedDecimalCodec.decode(b, off + 12, 10, 2));
        }

        public byte[] toBytes(CobolCharset cs) {
            byte[] out = new byte[LENGTH];
            System.arraycopy(ZonedDecimalCodec.encodeSigned(currBal, 10, 2, cs), 0, out, 0, 12);
            System.arraycopy(PackedDecimalCodec.encode(currCycDebit, 10, 2), 0, out, 12, 7);
            return out;
        }
    }

    public ArrArrayRec {
        if (bal.length != OCCURS) {
            throw new IllegalArgumentException("ARR-ACCT-BAL must have " + OCCURS + " occurrences, got " + bal.length);
        }
        bal = bal.clone();
    }

    /** Occurrence {@code index} (0-based; COBOL subscript = index + 1). */
    public ArrAcctBal bal(int index) {
        return bal[index];
    }

    /** INITIALIZE ARR-ARRAY-REC: zero id, +0 balances, +0 packed debits, space filler. */
    public static ArrArrayRec initialized() {
        ArrAcctBal[] bals = new ArrAcctBal[OCCURS];
        Arrays.fill(bals, ArrAcctBal.initialized());
        return new ArrArrayRec("00000000000", bals, TextCodec.spaces(4));
    }

    /** Returns a copy with occurrence {@code index} (0-based) replaced. */
    public ArrArrayRec withBal(int index, ArrAcctBal value) {
        ArrAcctBal[] bals = bal.clone();
        bals[index] = value;
        return new ArrArrayRec(acctId, bals, filler);
    }

    public ArrArrayRec withAcctId(String newAcctId) {
        return new ArrArrayRec(newAcctId, bal, filler);
    }

    public static ArrArrayRec fromBytes(byte[] b, CobolCharset cs) {
        if (b.length != LENGTH) {
            throw new IllegalArgumentException("ARR-ARRAY-REC must be " + LENGTH + " bytes, got " + b.length);
        }
        ArrAcctBal[] bals = new ArrAcctBal[OCCURS];
        for (int i = 0; i < OCCURS; i++) {
            bals[i] = ArrAcctBal.fromBytes(b, 11 + i * ArrAcctBal.LENGTH, cs);
        }
        return new ArrArrayRec(
                ZonedDecimalCodec.decodeUnsigned(b, 0, 11, cs),
                bals,
                TextCodec.decode(b, 106, 4, cs));
    }

    public byte[] toBytes(CobolCharset cs) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(LENGTH);
        out.writeBytes(ZonedDecimalCodec.encodeUnsigned(acctId, cs));
        for (ArrAcctBal b : bal) {
            out.writeBytes(b.toBytes(cs));
        }
        out.writeBytes(TextCodec.encode(filler, 4, cs));
        return out.toByteArray();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ArrArrayRec that
                && acctId.equals(that.acctId)
                && Arrays.equals(bal, that.bal)
                && filler.equals(that.filler);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * acctId.hashCode() + Arrays.hashCode(bal)) + filler.hashCode();
    }

    @Override
    public String toString() {
        return "ArrArrayRec[acctId=" + acctId + ", bal=" + Arrays.toString(bal) + ", filler=" + filler + "]";
    }
}
