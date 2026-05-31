package com.carddemo.backend.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cards")
public class CardEntity {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "card_acct_id")
    private Long cardAcctId;

    @Column(name = "cvv_cd")
    private Integer cvvCd;

    @Column(name = "embossed_name", length = 50)
    private String embossedName;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "active_status", length = 1)
    private String activeStatus;

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public Long getCardAcctId() { return cardAcctId; }
    public void setCardAcctId(Long cardAcctId) { this.cardAcctId = cardAcctId; }

    public Integer getCvvCd() { return cvvCd; }
    public void setCvvCd(Integer cvvCd) { this.cvvCd = cvvCd; }

    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
