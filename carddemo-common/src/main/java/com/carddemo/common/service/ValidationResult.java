package com.carddemo.common.service;

/**
 * Result of a transaction posting validation (CBTRN02C business rules).
 */
public class ValidationResult {

    private final boolean accepted;
    private final Long accountId;
    private final int reasonCode;
    private final String reasonDescription;

    private ValidationResult(boolean accepted, Long accountId, int reasonCode, String reasonDescription) {
        this.accepted = accepted;
        this.accountId = accountId;
        this.reasonCode = reasonCode;
        this.reasonDescription = reasonDescription;
    }

    public static ValidationResult accepted(Long accountId) {
        return new ValidationResult(true, accountId, 0, null);
    }

    public static ValidationResult rejected(int reasonCode, String reasonDescription) {
        return new ValidationResult(false, null, reasonCode, reasonDescription);
    }

    public boolean isAccepted() { return accepted; }
    public Long getAccountId() { return accountId; }
    public int getReasonCode() { return reasonCode; }
    public String getReasonDescription() { return reasonDescription; }
}
