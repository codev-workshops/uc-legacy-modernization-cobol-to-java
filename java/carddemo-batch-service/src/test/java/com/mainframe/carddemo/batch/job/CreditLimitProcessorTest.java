package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.DailyTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditLimitProcessorTest {

    private CreditLimitProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CreditLimitProcessor();
    }

    @Test
    void process_withinLimit_passes() throws Exception {
        PostingResult input = createResult(
                new BigDecimal("100.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("50.00"));

        PostingResult result = processor.process(input);

        assertFalse(result.isRejected());
    }

    @Test
    void process_exceedsLimit_rejects102() throws Exception {
        PostingResult input = createResult(
                new BigDecimal("5000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("4900.00"),
                new BigDecimal("50.00"));

        PostingResult result = processor.process(input);

        assertTrue(result.isRejected());
        assertEquals(102, result.getRejectReason());
    }

    @Test
    void process_exactLimit_passes() throws Exception {
        PostingResult input = createResult(
                new BigDecimal("50.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("4900.00"),
                new BigDecimal("50.00"));

        PostingResult result = processor.process(input);

        assertFalse(result.isRejected());
    }

    @Test
    void process_alreadyRejected_passesThrough() throws Exception {
        DailyTransaction txn = new DailyTransaction();
        txn.setTranId("T001");
        txn.setTranAmt(new BigDecimal("100.00"));
        PostingResult input = new PostingResult(txn);
        input.reject(100, "Already rejected");

        PostingResult result = processor.process(input);

        assertTrue(result.isRejected());
        assertEquals(100, result.getRejectReason());
    }

    private PostingResult createResult(BigDecimal tranAmt, BigDecimal creditLimit,
                                        BigDecimal cycCredit, BigDecimal cycDebit) {
        DailyTransaction txn = new DailyTransaction();
        txn.setTranId("T001");
        txn.setTranAmt(tranAmt);
        PostingResult result = new PostingResult(txn);
        result.setCreditLimit(creditLimit);
        result.setCurrentCycleCredit(cycCredit);
        result.setCurrentCycleDebit(cycDebit);
        return result;
    }
}
