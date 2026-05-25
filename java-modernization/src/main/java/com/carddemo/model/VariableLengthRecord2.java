package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL VBRC-REC2 (variable-length record type 2, 39 bytes).
 *
 * <pre>
 * 05 VB2-ACCT-ID               PIC 9(11)
 * 05 VB2-ACCT-CURR-BAL         PIC S9(10)V99
 * 05 VB2-ACCT-CREDIT-LIMIT     PIC S9(10)V99
 * 05 VB2-ACCT-REISSUE-YYYY     PIC X(04)
 * </pre>
 */
public record VariableLengthRecord2(
        String acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYear
) {
    public static final int RECORD_LENGTH = 39;
}
