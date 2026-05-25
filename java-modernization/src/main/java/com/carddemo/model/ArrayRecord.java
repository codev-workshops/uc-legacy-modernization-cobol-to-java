package com.carddemo.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps to COBOL FD ARRY-FILE record ARR-ARRAY-REC.
 *
 * <pre>
 * 05 ARR-ACCT-ID                  PIC 9(11)
 * 05 ARR-ACCT-BAL OCCURS 5 TIMES
 *    10 ARR-ACCT-CURR-BAL         PIC S9(10)V99
 *    10 ARR-ACCT-CURR-CYC-DEBIT   PIC S9(10)V99 COMP-3
 * 05 ARR-FILLER                   PIC X(04)
 * </pre>
 */
public record ArrayRecord(
        String acctId,
        List<BalanceEntry> balanceEntries,
        String filler
) {
    public record BalanceEntry(BigDecimal currBal, BigDecimal currCycDebit) {
    }
}
