package com.carddemo.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardXrefDto {

    @NotNull
    @Size(max = 16)
    private String xrefCardNum;

    @NotNull
    private Long xrefCustId;

    @NotNull
    private Long xrefAcctId;
}
