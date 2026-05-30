package com.mainframe.carddemo.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "tran_cat_balance")
@IdClass(TranCatBalanceId.class)
public class TranCatBalance {

    @Id
    @Column(name = "trancat_acct_id", nullable = false)
    private Long trancatAcctId;

    @Id
    @Column(name = "trancat_type_cd", length = 2, nullable = false)
    private String trancatTypeCd;

    @Id
    @Column(name = "trancat_cd", nullable = false)
    private Integer trancatCd;

    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal tranCatBal;

    public TranCatBalance() {}

    public Long getTrancatAcctId() { return trancatAcctId; }
    public void setTrancatAcctId(Long trancatAcctId) { this.trancatAcctId = trancatAcctId; }

    public String getTrancatTypeCd() { return trancatTypeCd; }
    public void setTrancatTypeCd(String trancatTypeCd) { this.trancatTypeCd = trancatTypeCd; }

    public Integer getTrancatCd() { return trancatCd; }
    public void setTrancatCd(Integer trancatCd) { this.trancatCd = trancatCd; }

    public BigDecimal getTranCatBal() { return tranCatBal; }
    public void setTranCatBal(BigDecimal tranCatBal) { this.tranCatBal = tranCatBal; }
}
