package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Modernized from COBOL copybook CVACT03Y.cpy — Card cross-reference (RECLN 50).
 */
@Entity
@Table(name = "card_xref")
public class CardCrossReference {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNumber;

    @Column(name = "cust_id")
    private Long customerId;

    @Column(name = "acct_id")
    private Long accountId;

    public CardCrossReference() {}

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
}
