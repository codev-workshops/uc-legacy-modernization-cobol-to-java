package com.carddemo.common.dto;

import java.time.LocalDate;

public class CardDto {

    private String cardNumber;
    private Long accountId;
    private Long customerId;
    private String cardStatus;
    private LocalDate expirationDate;
    private Integer cvvCode;
    private String embossedName;

    public CardDto() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(String cardStatus) {
        this.cardStatus = cardStatus;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getCvvCode() {
        return cvvCode;
    }

    public void setCvvCode(Integer cvvCode) {
        this.cvvCode = cvvCode;
    }

    public String getEmbossedName() {
        return embossedName;
    }

    public void setEmbossedName(String embossedName) {
        this.embossedName = embossedName;
    }
}
