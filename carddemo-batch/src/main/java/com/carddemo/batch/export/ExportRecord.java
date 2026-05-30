package com.carddemo.batch.export;

/**
 * Java representation of a single record in the polymorphic export file.
 * Maps to the CVEXPORT.cpy layout: a common header (type, timestamp, sequence,
 * branch, region) followed by entity-specific data fields.
 */
public class ExportRecord {

    private RecordType recordType;
    private String timestamp;
    private long sequenceNum;
    private String branchId;
    private String regionCode;
    private String[] fields;

    public ExportRecord() {}

    public ExportRecord(RecordType recordType, String timestamp, long sequenceNum,
                        String branchId, String regionCode, String[] fields) {
        this.recordType = recordType;
        this.timestamp = timestamp;
        this.sequenceNum = sequenceNum;
        this.branchId = branchId;
        this.regionCode = regionCode;
        this.fields = fields;
    }

    public RecordType getRecordType() { return recordType; }
    public void setRecordType(RecordType recordType) { this.recordType = recordType; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public long getSequenceNum() { return sequenceNum; }
    public void setSequenceNum(long sequenceNum) { this.sequenceNum = sequenceNum; }
    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
    public String[] getFields() { return fields; }
    public void setFields(String[] fields) { this.fields = fields; }
}
