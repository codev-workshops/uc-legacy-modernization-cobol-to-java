package com.carddemo.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    @NotNull
    private Long acctId;

    @Size(max = 1)
    private String acctActiveStatus;

    private BigDecimal acctCurrBal;

    private BigDecimal acctCreditLimit;

    private BigDecimal acctCashCreditLimit;

    @Size(max = 10)
    private String acctOpenDate;

    @Size(max = 10)
    private String acctExpirationDate;

    @Size(max = 10)
    private String acctReissueDate;

    private BigDecimal acctCurrCycCredit;

    private BigDecimal acctCurrCycDebit;

    @Size(max = 10)
    private String acctAddrZip;

    @Size(max = 10)
    private String acctGroupId;
}
