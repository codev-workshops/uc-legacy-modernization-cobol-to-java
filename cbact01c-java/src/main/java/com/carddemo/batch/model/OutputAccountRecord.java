package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL FD OUT-ACCT-REC.
 * Sequential output record with account summary data and formatted reissue date.
 */
public record OutputAccountRecord(
        long acctId,                   // PIC 9(11)
        String activeStatus,           // PIC X(01)
        BigDecimal currentBalance,     // PIC S9(10)V99
        BigDecimal creditLimit,        // PIC S9(10)V99
        BigDecimal cashCreditLimit,    // PIC S9(10)V99
        String openDate,               // PIC X(10)
        String expirationDate,         // PIC X(10)
        String reissueDate,            // PIC X(10) - formatted by COBDATFT
        BigDecimal currentCycleCredit, // PIC S9(10)V99
        BigDecimal currentCycleDebit,  // PIC S9(10)V99 COMP-3
        String groupId                 // PIC X(10)
) {

    /**
     * Formats this record as a pipe-delimited line for the output file.
     */
    public String toDelimitedString() {
        return String.join("|",
                String.format("%011d", acctId),
                activeStatus,
                currentBalance.toPlainString(),
                creditLimit.toPlainString(),
                cashCreditLimit.toPlainString(),
                openDate,
                expirationDate,
                reissueDate,
                currentCycleCredit.toPlainString(),
                currentCycleDebit.toPlainString(),
                groupId
        );
    }
}
