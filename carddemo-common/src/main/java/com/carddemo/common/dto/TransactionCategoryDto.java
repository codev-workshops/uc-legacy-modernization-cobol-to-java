package com.carddemo.common.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryDto {

    @Size(max = 2)
    private String tranTypeCd;

    private Integer tranCatCd;

    @Size(max = 50)
    private String tranCatTypeDesc;
}
