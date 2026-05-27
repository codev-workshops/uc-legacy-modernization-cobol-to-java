package com.carddemo.entity;

/**
 * Migrated from CVTRA03Y.cpy — TRAN-TYPE-RECORD (60-byte record).
 */
public record TranTypeRecord(
        String tranType,        // PIC X(02)
        String tranTypeDesc     // PIC X(50)
) {
}
