package com.carddemo.entity;

import java.time.LocalDateTime;

/**
 * Migrated from CVEXPORT.cpy — EXPORT-RECORD (500-byte record).
 * Common header fields plus a polymorphic data section using sealed interface.
 */
public class ExportRecord {

    private String recType;                 // PIC X(1)
    private LocalDateTime timestamp;        // PIC X(26) timestamp
    private int sequenceNum;                // PIC 9(9) COMP
    private String branchId;                // PIC X(4)
    private String regionCode;              // PIC X(5)
    private ExportRecordData recordData;    // REDEFINES union

    public ExportRecord() {
    }

    public String getRecType() {
        return recType;
    }

    public void setRecType(String recType) {
        this.recType = recType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(int sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public ExportRecordData getRecordData() {
        return recordData;
    }

    public void setRecordData(ExportRecordData recordData) {
        this.recordData = recordData;
    }
}
