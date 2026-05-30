package com.carddemo.batch.cbact01c.model;

/**
 * Short variable-length record for VBRCFILE (12 bytes).
 * Corresponds to VBRC-REC1 in CBACT01C.cbl.
 */
public class VbRecord1 {

    private String acctId;       // PIC 9(11)
    private String activeStatus; // PIC X(01)

    public VbRecord1() {
    }

    public VbRecord1(String acctId, String activeStatus) {
        this.acctId = acctId;
        this.activeStatus = activeStatus;
    }

    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
