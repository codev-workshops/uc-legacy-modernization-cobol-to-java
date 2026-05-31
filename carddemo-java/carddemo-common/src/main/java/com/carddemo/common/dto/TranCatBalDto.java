package com.carddemo.common.dto;

import java.math.BigDecimal;

public class TranCatBalDto {

    private Long accountId;
    private Integer tranTypeCode;
    private Integer tranCategoryCode;
    private BigDecimal balance;

    public TranCatBalDto() {
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getTranTypeCode() {
        return tranTypeCode;
    }

    public void setTranTypeCode(Integer tranTypeCode) {
        this.tranTypeCode = tranTypeCode;
    }

    public Integer getTranCategoryCode() {
        return tranCategoryCode;
    }

    public void setTranCategoryCode(Integer tranCategoryCode) {
        this.tranCategoryCode = tranCategoryCode;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
