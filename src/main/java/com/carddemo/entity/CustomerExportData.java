package com.carddemo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Migrated from CVEXPORT.cpy — EXPORT-CUSTOMER-DATA REDEFINES.
 */
public record CustomerExportData(
        int expCustId,                      // PIC 9(09) COMP
        String expCustFirstName,            // PIC X(25)
        String expCustMiddleName,           // PIC X(25)
        String expCustLastName,             // PIC X(25)
        List<String> expCustAddrLines,      // OCCURS 3 TIMES PIC X(50)
        String expCustAddrStateCd,          // PIC X(02)
        String expCustAddrCountryCd,        // PIC X(03)
        String expCustAddrZip,              // PIC X(10)
        List<String> expCustPhoneNums,      // OCCURS 2 TIMES PIC X(15)
        // PII — requires masking/encryption
        int expCustSsn,                     // PIC 9(09)
        String expCustGovtIssuedId,         // PIC X(20)
        LocalDate expCustDobYyyyMmDd,       // PIC X(10) date
        String expCustEftAccountId,         // PIC X(10)
        String expCustPriCardHolderInd,     // PIC X(01)
        BigDecimal expCustFicoCreditScore   // PIC 9(03) COMP-3
) implements ExportRecordData {
}
