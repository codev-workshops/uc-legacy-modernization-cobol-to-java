package com.carddemo.common.entity;

import java.io.Serializable;
import java.util.Objects;

public class DailyTransactionId implements Serializable {

    private String tranId;
    private String cardNum;
    private String origTs;

    public DailyTransactionId() {}

    public DailyTransactionId(String tranId, String cardNum, String origTs) {
        this.tranId = tranId;
        this.cardNum = cardNum;
        this.origTs = origTs;
    }

    public String getTranId() { return tranId; }
    public void setTranId(String tranId) { this.tranId = tranId; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public String getOrigTs() { return origTs; }
    public void setOrigTs(String origTs) { this.origTs = origTs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DailyTransactionId that)) return false;
        return Objects.equals(tranId, that.tranId)
                && Objects.equals(cardNum, that.cardNum)
                && Objects.equals(origTs, that.origTs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tranId, cardNum, origTs);
    }
}
