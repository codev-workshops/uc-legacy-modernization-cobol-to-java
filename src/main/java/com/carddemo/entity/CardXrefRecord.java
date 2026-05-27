package com.carddemo.entity;

/**
 * Migrated from CVACT03Y.cpy — CARD-XREF-RECORD (50-byte cross-reference).
 */
public record CardXrefRecord(
        String xrefCardNum,     // PIC X(16)
        int xrefCustId,         // PIC 9(09)
        long xrefAcctId         // PIC 9(11)
) {
}
