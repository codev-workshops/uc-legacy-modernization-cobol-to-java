package com.carddemo.online.dto;

import java.math.BigDecimal;
import java.util.List;

public class AccountResponse {

    private Long acctId;
    private String activeStatus;
    private BigDecimal currBal;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private String openDate;
    private String expirationDate;
    private String reissueDate;
    private BigDecimal currCycCredit;
    private BigDecimal currCycDebit;
    private String addrZip;
    private String groupId;
    private List<CardSummary> cards;
    private CustomerSummary customer;

    public AccountResponse() {}

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
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
    public String getAddrZip() { return addrZip; }
    public void setAddrZip(String addrZip) { this.addrZip = addrZip; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public List<CardSummary> getCards() { return cards; }
    public void setCards(List<CardSummary> cards) { this.cards = cards; }
    public CustomerSummary getCustomer() { return customer; }
    public void setCustomer(CustomerSummary customer) { this.customer = customer; }

    public static class CardSummary {
        private String cardNum;
        private String embossedName;
        private String activeStatus;
        private String expirationDate;

        public CardSummary() {}

        public CardSummary(String cardNum, String embossedName, String activeStatus,
                           String expirationDate) {
            this.cardNum = cardNum;
            this.embossedName = embossedName;
            this.activeStatus = activeStatus;
            this.expirationDate = expirationDate;
        }

        public String getCardNum() { return cardNum; }
        public void setCardNum(String cardNum) { this.cardNum = cardNum; }
        public String getEmbossedName() { return embossedName; }
        public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }
        public String getActiveStatus() { return activeStatus; }
        public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
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
