package com.mainframe.carddemo.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "card")
public class Card {

    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "card_acct_id")
    private Long cardAcctId;

    @Column(name = "card_cvv_cd")
    private Integer cardCvvCd;

    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    @Column(name = "card_expiration_date")
    private LocalDate cardExpirationDate;

    @Column(name = "card_active_status", length = 1)
    private String cardActiveStatus;

    public Card() {}

    public String getCardNum() { return cardNum; }
    public void setCardNum(String v) { this.cardNum = v; }

    public Long getCardAcctId() { return cardAcctId; }
    public void setCardAcctId(Long v) { this.cardAcctId = v; }

    public Integer getCardCvvCd() { return cardCvvCd; }
    public void setCardCvvCd(Integer v) { this.cardCvvCd = v; }

    public String getCardEmbossedName() { return cardEmbossedName; }
    public void setCardEmbossedName(String v) { this.cardEmbossedName = v; }

    public LocalDate getCardExpirationDate() { return cardExpirationDate; }
    public void setCardExpirationDate(LocalDate v) { this.cardExpirationDate = v; }

    public String getCardActiveStatus() { return cardActiveStatus; }
    public void setCardActiveStatus(String v) { this.cardActiveStatus = v; }
}
