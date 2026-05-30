package com.carddemo.common.dto;

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
public class DisclosureGroupDto {

    @Size(max = 10)
    private String disAcctGroupId;

    @Size(max = 2)
    private String disTranTypeCd;

    private Integer disTranCatCd;

    private BigDecimal disIntRate;
}
