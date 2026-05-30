package com.carddemo.online.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TransactionRequest {

    @NotBlank
    @Size(max = 16)
    private String cardNum;

    @Size(max = 2)
    private String typeCd;

    private Integer catCd;

    @Size(max = 10)
    private String source;

    @Size(max = 100)
    private String description;

    @NotNull
    private BigDecimal amt;

    private Long merchantId;

    @Size(max = 50)
    private String merchantName;

    @Size(max = 50)
    private String merchantCity;

    @Size(max = 10)
    private String merchantZip;

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }
    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmt() { return amt; }
    public void setAmt(BigDecimal amt) { this.amt = amt; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String merchantCity) { this.merchantCity = merchantCity; }
    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String merchantZip) { this.merchantZip = merchantZip; }
}
