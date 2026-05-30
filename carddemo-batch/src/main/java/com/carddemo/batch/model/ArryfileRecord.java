package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to ARR-ARRAY-REC from CBACT01C.cbl lines 72-78.
 * 5 array slots, each with (balance, cycDebit). Slots 4-5 are always zero.
 */
public class ArryfileRecord {

    private long acctId;
    private final BigDecimal[] balances = new BigDecimal[5];
    private final BigDecimal[] cycDebits = new BigDecimal[5];

    public ArryfileRecord() {
        for (int i = 0; i < 5; i++) {
            balances[i] = BigDecimal.ZERO;
            cycDebits[i] = BigDecimal.ZERO;
        }
    }

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }

    public BigDecimal getBalance(int index) { return balances[index]; }
    public void setBalance(int index, BigDecimal value) { balances[index] = value; }

    public BigDecimal getCycDebit(int index) { return cycDebits[index]; }
    public void setCycDebit(int index, BigDecimal value) { cycDebits[index] = value; }

    public BigDecimal[] getBalances() { return balances; }
    public BigDecimal[] getCycDebits() { return cycDebits; }
}
