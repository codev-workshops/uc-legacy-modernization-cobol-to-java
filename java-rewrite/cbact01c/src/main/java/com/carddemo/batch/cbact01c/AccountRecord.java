package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;

/**
 * Data model matching CVACT01Y.cpy — 300-byte ACCOUNT-RECORD layout.
 */
public record AccountRecord(
        long acctId,
        String acctActiveStatus,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        BigDecimal acctCashCreditLimit,
        String acctOpenDate,
        String acctExpirationDate,
        String acctReissueDate,
        BigDecimal acctCurrCycCredit,
        BigDecimal acctCurrCycDebit,
        String acctAddrZip,
        String acctGroupId
) {}
