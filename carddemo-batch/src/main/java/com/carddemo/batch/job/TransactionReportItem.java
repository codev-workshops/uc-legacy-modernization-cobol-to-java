package com.carddemo.batch.job;

import java.math.BigDecimal;

public class TransactionReportItem {

    private String tranId;
    private String accountId;
    private String typeCd;
    private String typeDesc;
    private int catCd;
    private String catDesc;
    private String source;
    private BigDecimal amount;
    private String cardNum;

    public TransactionReportItem() {}

    public String getTranId() { return tranId; }
    public void setTranId(String tranId) { this.tranId = tranId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }
    public String getTypeDesc() { return typeDesc; }
    public void setTypeDesc(String typeDesc) { this.typeDesc = typeDesc; }
    public int getCatCd() { return catCd; }
    public void setCatCd(int catCd) { this.catCd = catCd; }
    public String getCatDesc() { return catDesc; }
    public void setCatDesc(String catDesc) { this.catDesc = catDesc; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
}
