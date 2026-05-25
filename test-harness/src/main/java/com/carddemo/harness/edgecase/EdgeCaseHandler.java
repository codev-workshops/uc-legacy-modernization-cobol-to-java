package com.carddemo.harness.edgecase;

import java.math.BigDecimal;

/**
 * Handles CBACT01C-specific edge cases discovered during COBOL analysis.
 */
public final class EdgeCaseHandler {

    private EdgeCaseHandler() {}

    /**
     * Edge case 1: When ACCT-CURR-CYC-DEBIT is zero, COBOL substitutes 2525.00.
     * (app/cbl/CBACT01C.cbl lines 236-238)
     */
    public static BigDecimal expectedCycDebit(BigDecimal inputDebit) {
        if (inputDebit.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("2525.00");
        }
        return inputDebit;
    }

    /**
     * Edge case 2: COBDATFT date formatting — only first 8 bytes are meaningful.
     * Remaining bytes of the 20-byte CODATECN-0UT-DATE field may contain garbage.
     * When comparing OUT-ACCT-REISSUE-DATE (PIC X(10)), only compare first 8 chars.
     */
    public static String normalizeReissueDate(String rawDate) {
        if (rawDate == null || rawDate.length() < 8) {
            return rawDate == null ? "" : rawDate;
        }
        return rawDate.substring(0, 8);
    }

    /**
     * Edge case 3: ARRY-FILE occurrences 4 and 5 are never populated by CBACT01C.
     * Only indices 1-3 are set in paragraph 1400-POPUL-ARRAY-RECORD.
     * These should contain binary zeros (COMP-3) or display zeros.
     */
    public static boolean isUnpopulatedArraySlot(int index) {
        return index > 3;
    }

    /**
     * Edge case 4: Hardcoded array values that must match exactly.
     * ARR-ACCT-CURR-CYC-DEBIT(1) = 1005.00
     * ARR-ACCT-CURR-CYC-DEBIT(2) = 1525.00
     * ARR-ACCT-CURR-BAL(3) = -1025.00
     * ARR-ACCT-CURR-CYC-DEBIT(3) = -2500.00
     */
    public static BigDecimal expectedArrayCycDebit(int index) {
        return switch (index) {
            case 1 -> new BigDecimal("1005.00");
            case 2 -> new BigDecimal("1525.00");
            case 3 -> new BigDecimal("-2500.00");
            default -> BigDecimal.ZERO;
        };
    }

    public static BigDecimal expectedArrayBalance(int index, BigDecimal acctCurrBal) {
        return switch (index) {
            case 1, 2 -> acctCurrBal;
            case 3 -> new BigDecimal("-1025.00");
            default -> BigDecimal.ZERO;
        };
    }
}
