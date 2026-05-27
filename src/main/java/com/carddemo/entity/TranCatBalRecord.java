package com.carddemo.entity;

import java.math.BigDecimal;

/**
 * Migrated from CVTRA01Y.cpy — TRAN-CAT-BAL-RECORD (50-byte record).
 * Composite key: acctId + typeCd + catCd.
 */
public record TranCatBalRecord(
        long trancatAcctId,     // PIC 9(11) — part of composite key
        String trancatTypeCd,   // PIC X(02) — part of composite key
        int trancatCd,          // PIC 9(04) — part of composite key
        BigDecimal tranCatBal   // PIC S9(09)V99
) {
}
