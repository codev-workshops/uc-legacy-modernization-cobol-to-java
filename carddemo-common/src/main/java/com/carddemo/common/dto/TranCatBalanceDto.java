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
public class TranCatBalanceDto {

    @NotNull
    private Long trancatAcctId;

    @Size(max = 2)
    private String trancatTypeCd;

    private Integer trancatCd;

    private BigDecimal tranCatBal;
}
