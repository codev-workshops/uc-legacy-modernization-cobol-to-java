package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.DailyTransaction;

import java.math.BigDecimal;

public class PostingResult {

    private final DailyTransaction transaction;
    private boolean rejected;
    private int rejectReason;
    private String rejectDesc;
    private Long accountId;
    private BigDecimal creditLimit;
    private BigDecimal currentCycleCredit;
    private BigDecimal currentCycleDebit;
    private java.time.LocalDate expirationDate;

    public PostingResult(DailyTransaction transaction) {
        this.transaction = transaction;
        this.rejected = false;
    }

    public DailyTransaction getTransaction() { return transaction; }

    public boolean isRejected() { return rejected; }

    public void reject(int reason, String desc) {
        this.rejected = true;
        this.rejectReason = reason;
        this.rejectDesc = desc;
    }

    public int getRejectReason() { return rejectReason; }
    public String getRejectDesc() { return rejectDesc; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getCurrentCycleCredit() { return currentCycleCredit; }
    public void setCurrentCycleCredit(BigDecimal currentCycleCredit) { this.currentCycleCredit = currentCycleCredit; }

    public BigDecimal getCurrentCycleDebit() { return currentCycleDebit; }
    public void setCurrentCycleDebit(BigDecimal currentCycleDebit) { this.currentCycleDebit = currentCycleDebit; }

    public java.time.LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(java.time.LocalDate expirationDate) { this.expirationDate = expirationDate; }
}
