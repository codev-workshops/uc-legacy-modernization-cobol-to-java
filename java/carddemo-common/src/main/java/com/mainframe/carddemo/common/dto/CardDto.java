package com.mainframe.carddemo.common.dto;

import java.time.LocalDate;

public class CardDto {

    private String cardNum;
    private Long accountId;
    private Integer cvvCode;
    private String embossedName;
    private LocalDate expirationDate;
    private String activeStatus;

    public CardDto() {
    }

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Integer getCvvCode() { return cvvCode; }
    public void setCvvCode(Integer cvvCode) { this.cvvCode = cvvCode; }

    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
