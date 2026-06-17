package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;
import java.util.List;

/**
 * Output DTO matching lines 72-78 of CBACT01C.cbl (ARR-ARRAY-REC).
 * Contains acctId + array of 5 balance/debit entries.
 */
public record ArrayRecord(
        long acctId,
        List<ArrayEntry> entries
) {
    /**
     * A single slot in the 5-element array (bal + debit).
     */
    public record ArrayEntry(
            BigDecimal currBal,
            BigDecimal currCycDebit
    ) {}
}
