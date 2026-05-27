package com.carddemo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Migrated from CVACT01Y.cpy — ACCOUNT-RECORD (300-byte record).
 */
public class AccountRecord {

    private long acctId;                    // PIC 9(11)
    private String acctActiveStatus;        // PIC X(01)
    private BigDecimal acctCurrBal;         // PIC S9(10)V99
    private BigDecimal acctCreditLimit;     // PIC S9(10)V99
    private BigDecimal acctCashCreditLimit; // PIC S9(10)V99
    private LocalDate acctOpenDate;         // PIC X(10) date
    private LocalDate acctExpirationDate;   // PIC X(10) date
    private LocalDate acctReissueDate;      // PIC X(10) date
    private BigDecimal acctCurrCycCredit;   // PIC S9(10)V99
    private BigDecimal acctCurrCycDebit;    // PIC S9(10)V99
    private String acctAddrZip;             // PIC X(10)
    private String acctGroupId;             // PIC X(10)

    public AccountRecord() {
    }

    public long getAcctId() {
        return acctId;
    }

    public void setAcctId(long acctId) {
        this.acctId = acctId;
    }

    public String getAcctActiveStatus() {
        return acctActiveStatus;
    }

    public void setAcctActiveStatus(String acctActiveStatus) {
        this.acctActiveStatus = acctActiveStatus;
    }

    public BigDecimal getAcctCurrBal() {
        return acctCurrBal;
    }

    public void setAcctCurrBal(BigDecimal acctCurrBal) {
        this.acctCurrBal = acctCurrBal;
    }

    public BigDecimal getAcctCreditLimit() {
        return acctCreditLimit;
    }

    public void setAcctCreditLimit(BigDecimal acctCreditLimit) {
        this.acctCreditLimit = acctCreditLimit;
    }

    public BigDecimal getAcctCashCreditLimit() {
        return acctCashCreditLimit;
    }

    public void setAcctCashCreditLimit(BigDecimal acctCashCreditLimit) {
        this.acctCashCreditLimit = acctCashCreditLimit;
    }

    public LocalDate getAcctOpenDate() {
        return acctOpenDate;
    }

    public void setAcctOpenDate(LocalDate acctOpenDate) {
        this.acctOpenDate = acctOpenDate;
    }

    public LocalDate getAcctExpirationDate() {
        return acctExpirationDate;
    }

    public void setAcctExpirationDate(LocalDate acctExpirationDate) {
        this.acctExpirationDate = acctExpirationDate;
    }

    public LocalDate getAcctReissueDate() {
        return acctReissueDate;
    }

    public void setAcctReissueDate(LocalDate acctReissueDate) {
        this.acctReissueDate = acctReissueDate;
    }

    public BigDecimal getAcctCurrCycCredit() {
        return acctCurrCycCredit;
    }

    public void setAcctCurrCycCredit(BigDecimal acctCurrCycCredit) {
        this.acctCurrCycCredit = acctCurrCycCredit;
    }

    public BigDecimal getAcctCurrCycDebit() {
        return acctCurrCycDebit;
    }

    public void setAcctCurrCycDebit(BigDecimal acctCurrCycDebit) {
        this.acctCurrCycDebit = acctCurrCycDebit;
    }

    public String getAcctAddrZip() {
        return acctAddrZip;
    }

    public void setAcctAddrZip(String acctAddrZip) {
        this.acctAddrZip = acctAddrZip;
    }

    public String getAcctGroupId() {
        return acctGroupId;
    }

    public void setAcctGroupId(String acctGroupId) {
        this.acctGroupId = acctGroupId;
    }
}
