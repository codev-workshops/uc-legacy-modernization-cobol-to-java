package com.carddemo.batch.cbact01c;

/**
 * Output DTO matching lines 123-126 of CBACT01C.cbl (VBRC-REC1).
 * Conceptual length: 12 bytes.
 */
public record VbrcRecord1(
        long acctId,
        String acctActiveStatus
) {}
