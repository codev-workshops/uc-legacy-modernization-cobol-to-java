package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Longer variable-length record for VBRCFILE (39 bytes).
 * Corresponds to VBRC-REC2 in CBACT01C.cbl.
 */
public class VbRecord2 {

    private String acctId;            // PIC 9(11)
    private BigDecimal currBal;       // PIC S9(10)V99
    private BigDecimal creditLimit;   // PIC S9(10)V99
    private String reissueYear;       // PIC X(04) — first 4 chars of reissue date

    public VbRecord2() {
    }

    public VbRecord2(String acctId, BigDecimal currBal, BigDecimal creditLimit,
                     String reissueYear) {
        this.acctId = acctId;
        this.currBal = currBal;
        this.creditLimit = creditLimit;
        this.reissueYear = reissueYear;
    }

    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }

    public BigDecimal getCurrBal() { return currBal; }
    public void setCurrBal(BigDecimal currBal) { this.currBal = currBal; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public String getReissueYear() { return reissueYear; }
    public void setReissueYear(String reissueYear) { this.reissueYear = reissueYear; }
}
