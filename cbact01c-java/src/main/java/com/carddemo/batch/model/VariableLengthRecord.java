package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL VBRC-REC1 and VBRC-REC2.
 * Variable-length records written to the VBRC output file.
 */
public sealed interface VariableLengthRecord {

    /**
     * Short record (12 bytes in COBOL): account ID + active status.
     * Maps to VBRC-REC1.
     */
    record ShortRecord(
            long acctId,           // PIC 9(11)
            String activeStatus    // PIC X(01)
    ) implements VariableLengthRecord {

        public String toDelimitedString() {
            return String.format("VB1|%011d|%s", acctId, activeStatus);
        }
    }

    /**
     * Long record (39 bytes in COBOL): account ID + balance + credit limit + reissue year.
     * Maps to VBRC-REC2.
     */
    record LongRecord(
            long acctId,              // PIC 9(11)
            BigDecimal currentBalance,// PIC S9(10)V99
            BigDecimal creditLimit,   // PIC S9(10)V99
            String reissueYear        // PIC X(04)
    ) implements VariableLengthRecord {

        public String toDelimitedString() {
            return String.format("VB2|%011d|%s|%s|%s",
                    acctId,
                    currentBalance.toPlainString(),
                    creditLimit.toPlainString(),
                    reissueYear);
        }
    }
}
