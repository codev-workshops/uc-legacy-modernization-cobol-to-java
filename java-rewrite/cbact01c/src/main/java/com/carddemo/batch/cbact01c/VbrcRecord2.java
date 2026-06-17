package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;

/**
 * Output DTO matching lines 127-130 of CBACT01C.cbl (VBRC-REC2).
 * Conceptual length: 39 bytes.
 */
public record VbrcRecord2(
        long acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYear
) {}
