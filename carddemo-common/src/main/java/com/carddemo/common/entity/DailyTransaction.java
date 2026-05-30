package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "daily_transactions")
@IdClass(DailyTransactionId.class)
public class DailyTransaction {

    @Id
    @Column(name = "tran_id", length = 16)
    @Size(max = 16)
    private String tranId;

    @Column(name = "type_cd", length = 2)
    @Size(max = 2)
    private String typeCd;

    @Column(name = "cat_cd")
    private Integer catCd;

    @Column(name = "source", length = 10)
    @Size(max = 10)
    private String source;

    @Column(name = "description", length = 100)
    @Size(max = 100)
    private String desc;

    @Column(name = "amt", precision = 11, scale = 2)
    private BigDecimal amt;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "merchant_name", length = 50)
    @Size(max = 50)
    private String merchantName;

    @Column(name = "merchant_city", length = 50)
    @Size(max = 50)
    private String merchantCity;

    @Column(name = "merchant_zip", length = 10)
    @Size(max = 10)
    private String merchantZip;

    @Id
    @Column(name = "card_num", length = 16)
    @Size(max = 16)
    private String cardNum;

    @Id
    @Column(name = "orig_ts", length = 26)
    @Size(max = 26)
    private String origTs;

    @Column(name = "proc_ts", length = 26)
    @Size(max = 26)
    private String procTs;

    public DailyTransaction() {}

    public String getTranId() { return tranId; }
    public void setTranId(String tranId) { this.tranId = tranId; }
    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }
    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
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
    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public String getOrigTs() { return origTs; }
    public void setOrigTs(String origTs) { this.origTs = origTs; }
    public String getProcTs() { return procTs; }
    public void setProcTs(String procTs) { this.procTs = procTs; }
}
