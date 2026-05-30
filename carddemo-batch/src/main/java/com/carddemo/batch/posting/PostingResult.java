package com.carddemo.batch.posting;

import com.carddemo.common.entity.DailyTransaction;

/**
 * Wrapper returned by the processor. If {@code rejected} is non-null the
 * transaction failed validation; otherwise the writer should post it.
 */
public class PostingResult {

    private final DailyTransaction source;
    private final RejectedTransaction rejected;

    private PostingResult(DailyTransaction source, RejectedTransaction rejected) {
        this.source = source;
        this.rejected = rejected;
    }

    public static PostingResult accepted(DailyTransaction source) {
        return new PostingResult(source, null);
    }

    public static PostingResult rejected(DailyTransaction source, int reasonCode, String reasonDesc) {
        return new PostingResult(source, new RejectedTransaction(source, reasonCode, reasonDesc));
    }

    public boolean isAccepted() { return rejected == null; }
    public DailyTransaction getSource() { return source; }
    public RejectedTransaction getRejected() { return rejected; }
}
