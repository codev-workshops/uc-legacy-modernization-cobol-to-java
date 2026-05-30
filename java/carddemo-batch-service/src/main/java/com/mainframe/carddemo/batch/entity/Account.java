package com.mainframe.carddemo.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @Column(name = "acct_id", nullable = false)
    private Long acctId;

    @Column(name = "acct_active_status", length = 1)
    private String acctActiveStatus;

    @Column(name = "acct_curr_bal", precision = 12, scale = 2)
    private BigDecimal acctCurrBal;

    @Column(name = "acct_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCreditLimit;

    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCashCreditLimit;

    @Column(name = "acct_open_date")
    private LocalDate acctOpenDate;

    @Column(name = "acct_expiration_date")
    private LocalDate acctExpirationDate;

    @Column(name = "acct_reissue_date")
    private LocalDate acctReissueDate;

    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycCredit;

    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycDebit;

    @Column(name = "acct_addr_zip", length = 10)
    private String acctAddrZip;

    @Column(name = "acct_group_id", length = 10)
    private String acctGroupId;

    public Account() {}

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }

    public String getAcctActiveStatus() { return acctActiveStatus; }
    public void setAcctActiveStatus(String s) { this.acctActiveStatus = s; }

    public BigDecimal getAcctCurrBal() { return acctCurrBal; }
    public void setAcctCurrBal(BigDecimal v) { this.acctCurrBal = v; }

    public BigDecimal getAcctCreditLimit() { return acctCreditLimit; }
    public void setAcctCreditLimit(BigDecimal v) { this.acctCreditLimit = v; }

    public BigDecimal getAcctCashCreditLimit() { return acctCashCreditLimit; }
    public void setAcctCashCreditLimit(BigDecimal v) { this.acctCashCreditLimit = v; }

    public LocalDate getAcctOpenDate() { return acctOpenDate; }
    public void setAcctOpenDate(LocalDate v) { this.acctOpenDate = v; }

    public LocalDate getAcctExpirationDate() { return acctExpirationDate; }
    public void setAcctExpirationDate(LocalDate v) { this.acctExpirationDate = v; }

    public LocalDate getAcctReissueDate() { return acctReissueDate; }
    public void setAcctReissueDate(LocalDate v) { this.acctReissueDate = v; }

    public BigDecimal getAcctCurrCycCredit() { return acctCurrCycCredit; }
    public void setAcctCurrCycCredit(BigDecimal v) { this.acctCurrCycCredit = v; }

    public BigDecimal getAcctCurrCycDebit() { return acctCurrCycDebit; }
    public void setAcctCurrCycDebit(BigDecimal v) { this.acctCurrCycDebit = v; }

    public String getAcctAddrZip() { return acctAddrZip; }
    public void setAcctAddrZip(String v) { this.acctAddrZip = v; }

    public String getAcctGroupId() { return acctGroupId; }
    public void setAcctGroupId(String v) { this.acctGroupId = v; }
}
