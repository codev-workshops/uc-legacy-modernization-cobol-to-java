package com.carddemo.batch.model;

/**
 * Java equivalent of the CVACT03Y copybook (CARD-XREF-RECORD).
 * Fixed-length 50-byte record: XREF-CARD-NUM(16) + XREF-CUST-ID(9) + XREF-ACCT-ID(11) + FILLER(14).
 */
public class CardXrefRecord {

    public static final int RECORD_LENGTH = 50;

    private final String xrefCardNum;   // PIC X(16), offset 0
    private final String xrefCustId;    // PIC 9(09), offset 16
    private final String xrefAcctId;    // PIC 9(11), offset 25
    private final String filler;        // PIC X(14), offset 36

    public CardXrefRecord(String rawRecord) {
        if (rawRecord.length() != RECORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Expected " + RECORD_LENGTH + "-byte record but got " + rawRecord.length());
        }
        this.xrefCardNum = rawRecord.substring(0, 16);
        this.xrefCustId = rawRecord.substring(16, 25);
        this.xrefAcctId = rawRecord.substring(25, 36);
        this.filler = rawRecord.substring(36, 50);
    }

    public String getXrefCardNum() {
        return xrefCardNum;
    }

    public String getXrefCustId() {
        return xrefCustId;
    }

    public String getXrefAcctId() {
        return xrefAcctId;
    }

    public String getFiller() {
        return filler;
    }

    @Override
    public String toString() {
        return xrefCardNum + xrefCustId + xrefAcctId + filler;
    }
}
