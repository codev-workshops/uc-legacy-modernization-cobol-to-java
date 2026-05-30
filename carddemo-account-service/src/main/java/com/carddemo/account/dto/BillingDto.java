package com.carddemo.account.dto;

import java.math.BigDecimal;

public record BillingDto(
    Long acctId,
    BigDecimal currentBalance,
    BigDecimal creditLimit,
    BigDecimal cashCreditLimit,
    BigDecimal cycleCredits,
    BigDecimal cycleDebits,
    BigDecimal availableCredit,
    String statementDate,
    String dueDate
) {}
