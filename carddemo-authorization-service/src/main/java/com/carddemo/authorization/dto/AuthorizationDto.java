package com.carddemo.authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationDto {

    private Integer authId;
    private String cardNum;
    private LocalDateTime authTs;
    private String authType;
    private String cardExpiryDate;
    private String messageType;
    private String messageSource;
    private String authIdCode;
    private String authRespCode;
    private String authRespReason;
    private String processingCode;
    private BigDecimal transactionAmt;
    private BigDecimal approvedAmt;
    private String merchantCategoryCode;
    private String acqrCountryCode;
    private Short posEntryMode;
    private String merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantState;
    private String merchantZip;
    private String transactionId;
    private String matchStatus;
    private Long acctId;
    private Long custId;
    private LocalDateTime createdAt;
}
