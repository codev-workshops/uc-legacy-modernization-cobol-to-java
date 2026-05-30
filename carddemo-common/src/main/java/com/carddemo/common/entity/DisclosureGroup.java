package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroupId.class)
public class DisclosureGroup {

    @Id
    @Column(name = "acct_group_id", length = 10)
    @Size(max = 10)
    private String acctGroupId;

    @Id
    @Column(name = "type_cd", length = 2)
    @Size(max = 2)
    private String typeCd;

    @Id
    @Column(name = "cat_cd")
    private Integer catCd;

    @Column(name = "int_rate", precision = 6, scale = 2)
    private BigDecimal intRate;

    public DisclosureGroup() {}

    public String getAcctGroupId() { return acctGroupId; }
    public void setAcctGroupId(String acctGroupId) { this.acctGroupId = acctGroupId; }
    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }
    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }
    public BigDecimal getIntRate() { return intRate; }
    public void setIntRate(BigDecimal intRate) { this.intRate = intRate; }
}
