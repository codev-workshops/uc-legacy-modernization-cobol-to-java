package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;

/**
 * Output DTO matching lines 57-69 of CBACT01C.cbl (OUT-ACCT-REC).
 */
public record OutAccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String groupId
) {}
