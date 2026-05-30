package com.mainframe.carddemo.common.dto;

public class CardXrefDto {

    private String cardNum;
    private Long customerId;
    private Long accountId;

    public CardXrefDto() {
    }

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
}
