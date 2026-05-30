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
public class TransactionDto {

    @Size(max = 16)
    private String tranId;

    @Size(max = 2)
    private String tranTypeCd;

    private Integer tranCatCd;

    @Size(max = 10)
    private String tranSource;

    @Size(max = 100)
    private String tranDesc;

    private BigDecimal tranAmt;

    private Long tranMerchantId;

    @Size(max = 50)
    private String tranMerchantName;

    @Size(max = 50)
    private String tranMerchantCity;

    @Size(max = 10)
    private String tranMerchantZip;

    @Size(max = 16)
    private String tranCardNum;

    @Size(max = 26)
    private String tranOrigTs;

    @Size(max = 26)
    private String tranProcTs;
}
