package com.carddemo.entity;

import java.time.LocalDate;

/**
 * Migrated from CVACT02Y.cpy — CARD-RECORD (150-byte record).
 */
public class CardRecord {

    // PII — requires masking in logs and external interfaces
    private String cardNum;                 // PIC X(16)
    private long cardAcctId;                // PIC 9(11)
    // PII — requires encryption at rest
    private String cardCvvCd;               // PIC 9(03) stored as String for PII handling
    private String cardEmbossedName;        // PIC X(50)
    private LocalDate cardExpirationDate;   // PIC X(10) date
    private String cardActiveStatus;        // PIC X(01)

    public CardRecord() {
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public long getCardAcctId() {
        return cardAcctId;
    }

    public void setCardAcctId(long cardAcctId) {
        this.cardAcctId = cardAcctId;
    }

    public String getCardCvvCd() {
        return cardCvvCd;
    }

    public void setCardCvvCd(String cardCvvCd) {
        this.cardCvvCd = cardCvvCd;
    }

    public String getCardEmbossedName() {
        return cardEmbossedName;
    }

    public void setCardEmbossedName(String cardEmbossedName) {
        this.cardEmbossedName = cardEmbossedName;
    }

    public LocalDate getCardExpirationDate() {
        return cardExpirationDate;
    }

    public void setCardExpirationDate(LocalDate cardExpirationDate) {
        this.cardExpirationDate = cardExpirationDate;
    }

    public String getCardActiveStatus() {
        return cardActiveStatus;
    }

    public void setCardActiveStatus(String cardActiveStatus) {
        this.cardActiveStatus = cardActiveStatus;
    }
}
