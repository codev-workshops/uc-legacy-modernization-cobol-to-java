package com.carddemo.batch.posting;

import com.carddemo.common.entity.DailyTransaction;

/**
 * Holds a rejected daily transaction together with the numeric reason code
 * and human-readable description, mirroring the COBOL WS-VALIDATION-TRAILER.
 */
public class RejectedTransaction {

    private final DailyTransaction transaction;
    private final int reasonCode;
    private final String reasonDesc;

    public RejectedTransaction(DailyTransaction transaction, int reasonCode, String reasonDesc) {
        this.transaction = transaction;
        this.reasonCode = reasonCode;
        this.reasonDesc = reasonDesc;
    }

    public DailyTransaction getTransaction() { return transaction; }
    public int getReasonCode() { return reasonCode; }
    public String getReasonDesc() { return reasonDesc; }

    @Override
    public String toString() {
        return String.format("REJECTED[%d] %s – %s", reasonCode, transaction.getTranId(), reasonDesc);
    }
}
