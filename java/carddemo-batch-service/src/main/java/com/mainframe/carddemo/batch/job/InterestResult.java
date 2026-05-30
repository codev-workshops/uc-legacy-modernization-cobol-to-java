package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.BatchTransaction;
import java.math.BigDecimal;

public class InterestResult {

    private final Long acctId;
    private final BigDecimal monthlyInterest;
    private final BatchTransaction interestTransaction;

    public InterestResult(Long acctId, BigDecimal monthlyInterest, BatchTransaction interestTransaction) {
        this.acctId = acctId;
        this.monthlyInterest = monthlyInterest;
        this.interestTransaction = interestTransaction;
    }

    public Long getAcctId() { return acctId; }
    public BigDecimal getMonthlyInterest() { return monthlyInterest; }
    public BatchTransaction getInterestTransaction() { return interestTransaction; }
}
