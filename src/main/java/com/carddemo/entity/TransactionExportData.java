package com.carddemo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Migrated from CVEXPORT.cpy — EXPORT-TRANSACTION-DATA REDEFINES.
 */
public record TransactionExportData(
        String expTranId,               // PIC X(16)
        String expTranTypeCd,           // PIC X(02)
        int expTranCatCd,               // PIC 9(04)
        String expTranSource,           // PIC X(10)
        String expTranDesc,             // PIC X(100)
        BigDecimal expTranAmt,          // PIC S9(09)V99 COMP-3
        int expTranMerchantId,          // PIC 9(09) COMP
        String expTranMerchantName,     // PIC X(50)
        String expTranMerchantCity,     // PIC X(50)
        String expTranMerchantZip,      // PIC X(10)
        // PII — requires masking
        String expTranCardNum,          // PIC X(16)
        LocalDateTime expTranOrigTs,    // PIC X(26) timestamp
        LocalDateTime expTranProcTs     // PIC X(26) timestamp
) implements ExportRecordData {
}
