package com.carddemo.common.dto;

public class CardXrefDto {

    private Long cardNumber;
    private Long customerId;
    private Long accountId;

    public CardXrefDto() {
    }

    public CardXrefDto(Long cardNumber, Long customerId, Long accountId) {
        this.cardNumber = cardNumber;
        this.customerId = customerId;
        this.accountId = accountId;
    }

    public Long getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(Long cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
