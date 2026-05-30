package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Output account record for OUTFILE (LRECL=107 equivalent).
 * Corresponds to OUT-ACCT-REC in CBACT01C.cbl.
 */
public class OutAccountRecord {

    private String acctId;            // PIC 9(11)
    private String activeStatus;      // PIC X(01)
    private BigDecimal currBal;       // PIC S9(10)V99
    private BigDecimal creditLimit;   // PIC S9(10)V99
    private BigDecimal cashCreditLimit; // PIC S9(10)V99
    private String openDate;          // PIC X(10) — unchanged from input
    private String expirationDate;    // PIC X(10) — unchanged from input
    private String reissueDate;       // YYYYMMDD format (converted from YYYY-MM-DD via COBDATFT)
    private BigDecimal currCycCredit; // PIC S9(10)V99
    private BigDecimal currCycDebit;  // PIC S9(10)V99 COMP-3; 2525.00 if original is zero
    private String groupId;           // PIC X(10)

    public OutAccountRecord() {
    }

    public OutAccountRecord(String acctId, String activeStatus, BigDecimal currBal,
                            BigDecimal creditLimit, BigDecimal cashCreditLimit,
                            String openDate, String expirationDate, String reissueDate,
                            BigDecimal currCycCredit, BigDecimal currCycDebit,
                            String groupId) {
        this.acctId = acctId;
        this.activeStatus = activeStatus;
        this.currBal = currBal;
        this.creditLimit = creditLimit;
        this.cashCreditLimit = cashCreditLimit;
        this.openDate = openDate;
        this.expirationDate = expirationDate;
        this.reissueDate = reissueDate;
        this.currCycCredit = currCycCredit;
        this.currCycDebit = currCycDebit;
        this.groupId = groupId;
    }

    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }

    public BigDecimal getCurrBal() { return currBal; }
    public void setCurrBal(BigDecimal currBal) { this.currBal = currBal; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getCashCreditLimit() { return cashCreditLimit; }
    public void setCashCreditLimit(BigDecimal cashCreditLimit) { this.cashCreditLimit = cashCreditLimit; }

    public String getOpenDate() { return openDate; }
    public void setOpenDate(String openDate) { this.openDate = openDate; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getReissueDate() { return reissueDate; }
    public void setReissueDate(String reissueDate) { this.reissueDate = reissueDate; }

    public BigDecimal getCurrCycCredit() { return currCycCredit; }
    public void setCurrCycCredit(BigDecimal currCycCredit) { this.currCycCredit = currCycCredit; }

    public BigDecimal getCurrCycDebit() { return currCycDebit; }
    public void setCurrCycDebit(BigDecimal currCycDebit) { this.currCycDebit = currCycDebit; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
}
