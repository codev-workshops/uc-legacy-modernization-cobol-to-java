package com.mainframe.carddemo.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "disclosure_group")
@IdClass(DisclosureGroupId.class)
public class DisclosureGroup {

    @Id
    @Column(name = "dis_acct_group_id", length = 10, nullable = false)
    private String disAcctGroupId;

    @Id
    @Column(name = "dis_tran_type_cd", length = 2, nullable = false)
    private String disTranTypeCd;

    @Id
    @Column(name = "dis_tran_cat_cd", nullable = false)
    private Integer disTranCatCd;

    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal disIntRate;

    public DisclosureGroup() {}

    public String getDisAcctGroupId() { return disAcctGroupId; }
    public void setDisAcctGroupId(String disAcctGroupId) { this.disAcctGroupId = disAcctGroupId; }

    public String getDisTranTypeCd() { return disTranTypeCd; }
    public void setDisTranTypeCd(String disTranTypeCd) { this.disTranTypeCd = disTranTypeCd; }

    public Integer getDisTranCatCd() { return disTranCatCd; }
    public void setDisTranCatCd(Integer disTranCatCd) { this.disTranCatCd = disTranCatCd; }

    public BigDecimal getDisIntRate() { return disIntRate; }
    public void setDisIntRate(BigDecimal disIntRate) { this.disIntRate = disIntRate; }
}
