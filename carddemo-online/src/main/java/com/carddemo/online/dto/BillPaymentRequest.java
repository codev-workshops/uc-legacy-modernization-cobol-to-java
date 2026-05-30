package com.carddemo.online.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class BillPaymentRequest {

    @NotNull
    private Long accountId;

    @NotNull
    @Positive
    private BigDecimal paymentAmount;

    private String paymentSource;

    public BillPaymentRequest() {}

    public BillPaymentRequest(Long accountId, BigDecimal paymentAmount, String paymentSource) {
        this.accountId = accountId;
        this.paymentAmount = paymentAmount;
        this.paymentSource = paymentSource;
    }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    public String getPaymentSource() { return paymentSource; }
    public void setPaymentSource(String paymentSource) { this.paymentSource = paymentSource; }
}
