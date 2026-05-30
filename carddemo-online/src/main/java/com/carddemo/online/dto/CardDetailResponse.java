package com.carddemo.online.dto;

import java.math.BigDecimal;

public class CardDetailResponse {

    private String cardNum;
    private Long acctId;
    private Integer cvvCd;
    private String embossedName;
    private String expirationDate;
    private String activeStatus;

    private AccountSummary account;
    private CustomerSummary customer;

    public CardDetailResponse() {}

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
    public Integer getCvvCd() { return cvvCd; }
    public void setCvvCd(Integer cvvCd) { this.cvvCd = cvvCd; }
    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
    public AccountSummary getAccount() { return account; }
    public void setAccount(AccountSummary account) { this.account = account; }
    public CustomerSummary getCustomer() { return customer; }
    public void setCustomer(CustomerSummary customer) { this.customer = customer; }

    public static class AccountSummary {

        private Long acctId;
        private String activeStatus;
        private BigDecimal currBal;
        private BigDecimal creditLimit;
        private String expirationDate;

        public AccountSummary() {}

        public AccountSummary(Long acctId, String activeStatus, BigDecimal currBal,
                              BigDecimal creditLimit, String expirationDate) {
            this.acctId = acctId;
            this.activeStatus = activeStatus;
            this.currBal = currBal;
            this.creditLimit = creditLimit;
            this.expirationDate = expirationDate;
        }

        public Long getAcctId() { return acctId; }
        public void setAcctId(Long acctId) { this.acctId = acctId; }
        public String getActiveStatus() { return activeStatus; }
        public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
        public BigDecimal getCurrBal() { return currBal; }
        public void setCurrBal(BigDecimal currBal) { this.currBal = currBal; }
        public BigDecimal getCreditLimit() { return creditLimit; }
        public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
        public String getExpirationDate() { return expirationDate; }
        public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    }

    public static class CustomerSummary {

        private Long custId;
        private String firstName;
        private String lastName;

        public CustomerSummary() {}

        public CustomerSummary(Long custId, String firstName, String lastName) {
            this.custId = custId;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public Long getCustId() { return custId; }
        public void setCustId(Long custId) { this.custId = custId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
}
