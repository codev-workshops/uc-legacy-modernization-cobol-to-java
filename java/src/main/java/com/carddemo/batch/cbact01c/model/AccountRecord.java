package com.carddemo.batch.cbact01c.model;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.codec.TextCodec;
import com.carddemo.batch.cbact01c.codec.ZonedDecimalCodec;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;

/** ACCOUNT-RECORD (CVACT01Y), 300 bytes. Text fields keep their fixed width. */
public record AccountRecord(
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
        String addrZip,
        String groupId,
        byte[] filler) {

    public static final int LENGTH = 300;
    public static final int FILLER_LENGTH = 178;

    public static AccountRecord fromBytes(byte[] b, CobolCharset cs) {
        if (b.length != LENGTH) {
            throw new IllegalArgumentException("ACCOUNT-RECORD must be " + LENGTH + " bytes, got " + b.length);
        }
        return new AccountRecord(
                ZonedDecimalCodec.decodeUnsigned(b, 0, 11, cs),
                TextCodec.decode(b, 11, 1, cs),
                ZonedDecimalCodec.decodeSigned(b, 12, 10, 2, cs),
                ZonedDecimalCodec.decodeSigned(b, 24, 10, 2, cs),
                ZonedDecimalCodec.decodeSigned(b, 36, 10, 2, cs),
                TextCodec.decode(b, 48, 10, cs),
                TextCodec.decode(b, 58, 10, cs),
                TextCodec.decode(b, 68, 10, cs),
                ZonedDecimalCodec.decodeSigned(b, 78, 10, 2, cs),
                ZonedDecimalCodec.decodeSigned(b, 90, 10, 2, cs),
                TextCodec.decode(b, 102, 10, cs),
                TextCodec.decode(b, 112, 10, cs),
                Arrays.copyOfRange(b, 122, LENGTH));
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
        out.writeBytes(ZonedDecimalCodec.encodeSigned(currCycDebit, 10, 2, cs));
        out.writeBytes(TextCodec.encode(addrZip, 10, cs));
        out.writeBytes(TextCodec.encode(groupId, 10, cs));
        byte[] f = filler == null ? new byte[0] : filler;
        if (f.length == FILLER_LENGTH) {
            out.writeBytes(f);
        } else {
            byte[] padded = new byte[FILLER_LENGTH];
            Arrays.fill(padded, cs.encode(" ")[0]);
            System.arraycopy(f, 0, padded, 0, Math.min(f.length, FILLER_LENGTH));
            out.writeBytes(padded);
        }
        return out.toByteArray();
    }

    /** Field-by-field equality; the filler is compared by content. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountRecord that)) {
            return false;
        }
        return acctId.equals(that.acctId)
                && activeStatus.equals(that.activeStatus)
                && currBal.equals(that.currBal)
                && creditLimit.equals(that.creditLimit)
                && cashCreditLimit.equals(that.cashCreditLimit)
                && openDate.equals(that.openDate)
                && expirationDate.equals(that.expirationDate)
                && reissueDate.equals(that.reissueDate)
                && currCycCredit.equals(that.currCycCredit)
                && currCycDebit.equals(that.currCycDebit)
                && addrZip.equals(that.addrZip)
                && groupId.equals(that.groupId)
                && Arrays.equals(filler, that.filler);
    }

    @Override
    public int hashCode() {
        return 31 * acctId.hashCode() + Arrays.hashCode(filler);
    }

    @Override
    public String toString() {
        return "AccountRecord[acctId=" + acctId + ", activeStatus=" + activeStatus + ", currBal=" + currBal
                + ", creditLimit=" + creditLimit + ", cashCreditLimit=" + cashCreditLimit + ", openDate=" + openDate
                + ", expirationDate=" + expirationDate + ", reissueDate=" + reissueDate + ", currCycCredit="
                + currCycCredit + ", currCycDebit=" + currCycDebit + ", addrZip=" + addrZip + ", groupId=" + groupId
                + ", filler=" + (filler == null ? "null" : filler.length + " bytes") + "]";
    }
}
