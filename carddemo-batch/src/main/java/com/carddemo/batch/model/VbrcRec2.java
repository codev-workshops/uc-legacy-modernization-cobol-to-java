package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to VBRC-REC2 from CBACT01C.cbl lines 127-130. Record length = 39.
 */
public class VbrcRec2 {

    private long acctId;
    private BigDecimal currBal;
    private BigDecimal creditLimit;
    private String reissueYyyy;

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }
    public BigDecimal getCurrBal() { return currBal; }
    public void setCurrBal(BigDecimal currBal) { this.currBal = currBal; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public String getReissueYyyy() { return reissueYyyy; }
    public void setReissueYyyy(String reissueYyyy) { this.reissueYyyy = reissueYyyy; }
}
