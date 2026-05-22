package com.carddemo.batch.cbact01c.model;

import java.util.List;

/**
 * Array output record for ARRYFILE (LRECL=110 equivalent).
 * Corresponds to ARR-ARRAY-REC in CBACT01C.cbl.
 * Contains an account ID and 5 array slots of (balance, debit).
 */
public class ArrayRecord {

    private String acctId; // PIC 9(11)
    private List<ArraySlot> slots; // OCCURS 5 TIMES

    public ArrayRecord() {
    }

    public ArrayRecord(String acctId, List<ArraySlot> slots) {
        this.acctId = acctId;
        this.slots = slots;
    }

    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }

    public List<ArraySlot> getSlots() { return slots; }
    public void setSlots(List<ArraySlot> slots) { this.slots = slots; }
}
