package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps to COBOL FD ARR-ARRAY-REC.
 * Contains an account ID and an array of 5 balance/debit pairs.
 * In the COBOL program, only the first 3 entries are populated.
 */
public record ArrayRecord(
        long acctId,                          // PIC 9(11)
        List<BalanceEntry> balanceEntries      // OCCURS 5 TIMES
) {

    public record BalanceEntry(
            BigDecimal currentBalance,         // PIC S9(10)V99
            BigDecimal currentCycleDebit       // PIC S9(10)V99 COMP-3
    ) {
        public static BalanceEntry zero() {
            return new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    /**
     * Formats this record as a pipe-delimited line for the array output file.
     */
    public String toDelimitedString() {
        String entries = balanceEntries.stream()
                .map(e -> e.currentBalance().toPlainString() + "," + e.currentCycleDebit().toPlainString())
                .collect(Collectors.joining("|"));
        return String.format("%011d", acctId) + "|" + entries;
    }
}
