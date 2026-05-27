package com.carddemo.entity;

/**
 * Migrated from CVEXPORT.cpy — sealed interface for REDEFINES pattern.
 * Each variant corresponds to a REDEFINES of EXPORT-RECORD-DATA.
 */
public sealed interface ExportRecordData
        permits CustomerExportData, AccountExportData, TransactionExportData,
                CardXrefExportData, CardExportData {
}
