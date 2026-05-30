package com.carddemo.online.dto;

import java.math.BigDecimal;

public class BillPaymentResponse {

    private String transactionId;
    private Long accountId;
    private BigDecimal paymentAmount;
    private BigDecimal newBalance;
    private String message;

    public BillPaymentResponse() {}

    public BillPaymentResponse(String transactionId, Long accountId,
                               BigDecimal paymentAmount, BigDecimal newBalance,
                               String message) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.paymentAmount = paymentAmount;
        this.newBalance = newBalance;
        this.message = message;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    public BigDecimal getNewBalance() { return newBalance; }
    public void setNewBalance(BigDecimal newBalance) { this.newBalance = newBalance; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
