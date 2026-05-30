package com.carddemo.online.dto;

public class CardResponse {

    private String cardNum;
    private Long acctId;
    private Integer cvvCd;
    private String embossedName;
    private String expirationDate;
    private String activeStatus;

    public CardResponse() {}

    public CardResponse(String cardNum, Long acctId, Integer cvvCd,
                        String embossedName, String expirationDate,
                        String activeStatus) {
        this.cardNum = cardNum;
        this.acctId = acctId;
        this.cvvCd = cvvCd;
        this.embossedName = embossedName;
        this.expirationDate = expirationDate;
        this.activeStatus = activeStatus;
    }

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
}
