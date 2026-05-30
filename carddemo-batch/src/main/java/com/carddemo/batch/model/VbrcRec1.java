package com.carddemo.batch.model;

/**
 * Maps to VBRC-REC1 from CBACT01C.cbl lines 123-126. Record length = 12.
 */
public class VbrcRec1 {

    private long acctId;
    private String activeStatus;

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
