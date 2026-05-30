package com.mainframe.carddemo.batch.job;

import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class CreditLimitProcessor implements ItemProcessor<PostingResult, PostingResult> {

    @Override
    public PostingResult process(PostingResult item) {
        if (item.isRejected()) {
            return item;
        }

        BigDecimal cycleCredit = item.getCurrentCycleCredit() != null ? item.getCurrentCycleCredit() : BigDecimal.ZERO;
        BigDecimal cycleDebit = item.getCurrentCycleDebit() != null ? item.getCurrentCycleDebit() : BigDecimal.ZERO;
        BigDecimal tranAmt = item.getTransaction().getTranAmt() != null ? item.getTransaction().getTranAmt() : BigDecimal.ZERO;

        BigDecimal tempBal = cycleCredit.subtract(cycleDebit).add(tranAmt);

        if (tempBal.compareTo(item.getCreditLimit()) > 0) {
            item.reject(102, "Transaction exceeds credit limit");
        }

        return item;
    }
}
