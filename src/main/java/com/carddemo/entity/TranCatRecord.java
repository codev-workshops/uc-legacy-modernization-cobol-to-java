package com.carddemo.entity;

/**
 * Migrated from CVTRA04Y.cpy — TRAN-CAT-RECORD (60-byte record).
 * Composite key: tranTypeCd + tranCatCd.
 */
public record TranCatRecord(
        String tranTypeCd,          // PIC X(02) — part of composite key
        int tranCatCd,              // PIC 9(04) — part of composite key
        String tranCatTypeDesc      // PIC X(50)
) {
}
