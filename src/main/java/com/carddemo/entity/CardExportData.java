package com.carddemo.entity;

/**
 * Migrated from CVEXPORT.cpy — EXPORT-CARD-DATA REDEFINES.
 */
public record CardExportData(
        // PII — requires masking
        String expCardNum,              // PIC X(16)
        long expCardAcctId,             // PIC 9(11) COMP
        // PII — requires encryption
        int expCardCvvCd,               // PIC 9(03) COMP
        String expCardEmbossedName,     // PIC X(50)
        String expCardExpirationDate,   // PIC X(10)
        String expCardActiveStatus      // PIC X(01)
) implements ExportRecordData {
}
