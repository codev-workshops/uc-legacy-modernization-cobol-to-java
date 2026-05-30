package com.carddemo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportDto {

    private String tranReportTransId;

    private String tranReportAccountId;

    private String tranReportTypeCd;

    private String tranReportTypeDesc;

    private Integer tranReportCatCd;

    private String tranReportCatDesc;

    private String tranReportSource;

    private BigDecimal tranReportAmt;
}
