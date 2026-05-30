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
public class CustomerDto {

    @NotNull
    private Long custId;

    @Size(max = 25)
    private String custFirstName;

    @Size(max = 25)
    private String custMiddleName;

    @Size(max = 25)
    private String custLastName;

    @Size(max = 50)
    private String custAddrLine1;

    @Size(max = 50)
    private String custAddrLine2;

    @Size(max = 50)
    private String custAddrLine3;

    @Size(max = 2)
    private String custAddrStateCd;

    @Size(max = 3)
    private String custAddrCountryCd;

    @Size(max = 10)
    private String custAddrZip;

    @Size(max = 15)
    private String custPhoneNum1;

    @Size(max = 15)
    private String custPhoneNum2;

    private Long custSsn;

    @Size(max = 20)
    private String custGovtIssuedId;

    @Size(max = 10)
    private String custDob;

    @Size(max = 10)
    private String custEftAccountId;

    @Size(max = 1)
    private String custPriCardHolderInd;

    private Integer custFicoCreditScore;
}
