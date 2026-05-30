package com.mainframe.carddemo.transaction.service;

import java.math.BigDecimal;

public class BillPaymentRequest {

    private String cardNum;
    private BigDecimal amount;

    public BillPaymentRequest() {
    }

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
