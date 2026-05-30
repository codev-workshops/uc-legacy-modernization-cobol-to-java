package com.mainframe.carddemo.transaction.service;

import java.math.BigDecimal;

public class TransactionCreateRequest {

    private String cardNum;
    private String tranTypeCd;
    private Integer tranCatCd;
    private BigDecimal tranAmt;
    private String tranMerchantName;
    private String tranMerchantCity;
    private String tranMerchantZip;
    private String tranSource;
    private String tranDesc;

    public TransactionCreateRequest() {
    }

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }

    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer tranCatCd) { this.tranCatCd = tranCatCd; }

    public BigDecimal getTranAmt() { return tranAmt; }
    public void setTranAmt(BigDecimal tranAmt) { this.tranAmt = tranAmt; }

    public String getTranMerchantName() { return tranMerchantName; }
    public void setTranMerchantName(String tranMerchantName) { this.tranMerchantName = tranMerchantName; }

    public String getTranMerchantCity() { return tranMerchantCity; }
    public void setTranMerchantCity(String tranMerchantCity) { this.tranMerchantCity = tranMerchantCity; }

    public String getTranMerchantZip() { return tranMerchantZip; }
    public void setTranMerchantZip(String tranMerchantZip) { this.tranMerchantZip = tranMerchantZip; }

    public String getTranSource() { return tranSource; }
    public void setTranSource(String tranSource) { this.tranSource = tranSource; }

    public String getTranDesc() { return tranDesc; }
    public void setTranDesc(String tranDesc) { this.tranDesc = tranDesc; }
}
