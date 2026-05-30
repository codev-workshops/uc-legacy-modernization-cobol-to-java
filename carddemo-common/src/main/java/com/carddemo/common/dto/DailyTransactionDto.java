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
public class DailyTransactionDto {

    @Size(max = 16)
    private String dalytranId;

    @Size(max = 2)
    private String dalytranTypeCd;

    private Integer dalytranCatCd;

    @Size(max = 10)
    private String dalytranSource;

    @Size(max = 100)
    private String dalytranDesc;

    private BigDecimal dalytranAmt;

    private Long dalytranMerchantId;

    @Size(max = 50)
    private String dalytranMerchantName;

    @Size(max = 50)
    private String dalytranMerchantCity;

    @Size(max = 10)
    private String dalytranMerchantZip;

    @Size(max = 16)
    private String dalytranCardNum;

    @Size(max = 26)
    private String dalytranOrigTs;

    @Size(max = 26)
    private String dalytranProcTs;
}
