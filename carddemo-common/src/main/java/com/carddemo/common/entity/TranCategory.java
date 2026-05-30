package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tran_categories")
@IdClass(TranCategoryId.class)
public class TranCategory {

    @Id
    @Column(name = "type_cd", length = 2)
    @Size(max = 2)
    private String typeCd;

    @Id
    @Column(name = "cat_cd")
    private Integer catCd;

    @Column(name = "tran_cat_type_desc", length = 50)
    @Size(max = 50)
    private String tranCatTypeDesc;

    public TranCategory() {}

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }
    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }
    public String getTranCatTypeDesc() { return tranCatTypeDesc; }
    public void setTranCatTypeDesc(String tranCatTypeDesc) { this.tranCatTypeDesc = tranCatTypeDesc; }
}
