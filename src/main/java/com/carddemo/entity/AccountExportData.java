package com.carddemo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Migrated from CVEXPORT.cpy — EXPORT-ACCOUNT-DATA REDEFINES.
 */
public record AccountExportData(
        long expAcctId,                     // PIC 9(11)
        String expAcctActiveStatus,         // PIC X(01)
        BigDecimal expAcctCurrBal,          // PIC S9(10)V99 COMP-3
        BigDecimal expAcctCreditLimit,      // PIC S9(10)V99
        BigDecimal expAcctCashCreditLimit,  // PIC S9(10)V99 COMP-3
        LocalDate expAcctOpenDate,          // PIC X(10) date
        LocalDate expAcctExpirationDate,    // PIC X(10) date
        LocalDate expAcctReissueDate,       // PIC X(10) date
        BigDecimal expAcctCurrCycCredit,    // PIC S9(10)V99
        BigDecimal expAcctCurrCycDebit,     // PIC S9(10)V99 COMP
        String expAcctAddrZip,              // PIC X(10)
        String expAcctGroupId               // PIC X(10)
) implements ExportRecordData {
}
