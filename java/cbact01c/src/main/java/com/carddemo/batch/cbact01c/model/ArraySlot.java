package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * A single slot in the array record. Each slot holds a balance and a cycle debit.
 * Corresponds to ARR-ACCT-BAL OCCURS 5 TIMES in CBACT01C.cbl.
 */
public class ArraySlot {

    private BigDecimal balance;  // PIC S9(10)V99
    private BigDecimal cycDebit; // PIC S9(10)V99 COMP-3

    public ArraySlot() {
        this.balance = BigDecimal.ZERO;
        this.cycDebit = BigDecimal.ZERO;
    }

    public ArraySlot(BigDecimal balance, BigDecimal cycDebit) {
        this.balance = balance;
        this.cycDebit = cycDebit;
    }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getCycDebit() { return cycDebit; }
    public void setCycDebit(BigDecimal cycDebit) { this.cycDebit = cycDebit; }
}
