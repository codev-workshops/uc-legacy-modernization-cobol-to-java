package com.mainframe.carddemo.common.dto;

import java.math.BigDecimal;

public class BalanceUpdateDto {

    private BigDecimal currentBalance;
    private BigDecimal currentCycleCredit;
    private BigDecimal currentCycleDebit;

    public BalanceUpdateDto() {
    }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public BigDecimal getCurrentCycleCredit() { return currentCycleCredit; }
    public void setCurrentCycleCredit(BigDecimal currentCycleCredit) { this.currentCycleCredit = currentCycleCredit; }

    public BigDecimal getCurrentCycleDebit() { return currentCycleDebit; }
    public void setCurrentCycleDebit(BigDecimal currentCycleDebit) { this.currentCycleDebit = currentCycleDebit; }
}
