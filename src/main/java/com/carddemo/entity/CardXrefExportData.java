package com.carddemo.entity;

/**
 * Migrated from CVEXPORT.cpy — EXPORT-CARD-XREF-DATA REDEFINES.
 */
public record CardXrefExportData(
        String expXrefCardNum,      // PIC X(16)
        int expXrefCustId,          // PIC 9(09)
        long expXrefAcctId          // PIC 9(11) COMP
) implements ExportRecordData {
}
