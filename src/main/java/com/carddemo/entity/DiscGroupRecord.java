package com.carddemo.entity;

import java.math.BigDecimal;

/**
 * Migrated from CVTRA02Y.cpy — DIS-GROUP-RECORD (50-byte record).
 * Disclosure group with interest rate.
 */
public record DiscGroupRecord(
        String disAcctGroupId,  // PIC X(10) — part of composite key
        String disTranTypeCd,   // PIC X(02) — part of composite key
        int disTranCatCd,       // PIC 9(04) — part of composite key
        BigDecimal disIntRate   // PIC S9(04)V99
) {
}
