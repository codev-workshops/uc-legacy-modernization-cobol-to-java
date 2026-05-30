package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.DailyTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpirationProcessorTest {

    private ExpirationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ExpirationProcessor();
    }

    @Test
    void process_beforeExpiration_passes() throws Exception {
        PostingResult input = createResult(
                LocalDateTime.of(2024, 6, 15, 10, 0),
                LocalDate.of(2025, 12, 31));

        PostingResult result = processor.process(input);

        assertFalse(result.isRejected());
    }

    @Test
    void process_afterExpiration_rejects103() throws Exception {
        PostingResult input = createResult(
                LocalDateTime.of(2026, 1, 15, 10, 0),
                LocalDate.of(2025, 12, 31));

        PostingResult result = processor.process(input);

        assertTrue(result.isRejected());
        assertEquals(103, result.getRejectReason());
    }

    @Test
    void process_onExpirationDate_passes() throws Exception {
        PostingResult input = createResult(
                LocalDateTime.of(2025, 12, 31, 23, 59),
                LocalDate.of(2025, 12, 31));

        PostingResult result = processor.process(input);

        assertFalse(result.isRejected());
    }

    @Test
    void process_alreadyRejected_passesThrough() throws Exception {
        DailyTransaction txn = new DailyTransaction();
        txn.setTranId("T001");
        PostingResult input = new PostingResult(txn);
        input.reject(100, "Already rejected");

        PostingResult result = processor.process(input);

        assertTrue(result.isRejected());
        assertEquals(100, result.getRejectReason());
    }

    private PostingResult createResult(LocalDateTime origTs, LocalDate expirationDate) {
        DailyTransaction txn = new DailyTransaction();
        txn.setTranId("T001");
        txn.setTranOrigTs(origTs);
        PostingResult result = new PostingResult(txn);
        result.setExpirationDate(expirationDate);
        return result;
    }
}
