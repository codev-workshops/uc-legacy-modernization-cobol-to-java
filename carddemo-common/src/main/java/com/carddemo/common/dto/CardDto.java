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
public class CardDto {

    @NotNull
    @Size(max = 16)
    private String cardNum;

    @NotNull
    private Long cardAcctId;

    private Integer cardCvvCd;

    @Size(max = 50)
    private String cardEmbossedName;

    @Size(max = 10)
    private String cardExpirationDate;

    @Size(max = 1)
    private String cardActiveStatus;
}
