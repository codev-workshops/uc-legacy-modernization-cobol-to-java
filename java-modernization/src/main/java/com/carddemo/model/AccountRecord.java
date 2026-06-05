package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL copybook CVACT01Y.cpy — ACCOUNT-RECORD (RECLN 300).
 *
 * <pre>
 * 05 ACCT-ID                  PIC 9(11)
 * 05 ACCT-ACTIVE-STATUS       PIC X(01)
 * 05 ACCT-CURR-BAL            PIC S9(10)V99
 * 05 ACCT-CREDIT-LIMIT        PIC S9(10)V99
 * 05 ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99
 * 05 ACCT-OPEN-DATE           PIC X(10)
 * 05 ACCT-EXPIRAION-DATE      PIC X(10)
 * 05 ACCT-REISSUE-DATE        PIC X(10)
 * 05 ACCT-CURR-CYC-CREDIT     PIC S9(10)V99
 * 05 ACCT-CURR-CYC-DEBIT      PIC S9(10)V99
 * 05 ACCT-ADDR-ZIP            PIC X(10)
 * 05 ACCT-GROUP-ID            PIC X(10)
 * 05 FILLER                   PIC X(178)
 * </pre>
 */
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
        String groupId
) {
    public static final int RECORD_LENGTH = 300;
}
