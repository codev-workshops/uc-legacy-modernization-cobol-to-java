package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tran_types")
public class TranType {

    @Id
    @Column(name = "tran_type", length = 2)
    @NotBlank
    @Size(max = 2)
    private String tranType;

    @Column(name = "tran_type_desc", length = 50)
    @Size(max = 50)
    private String tranTypeDesc;

    public TranType() {}

    public String getTranType() { return tranType; }
    public void setTranType(String tranType) { this.tranType = tranType; }
    public String getTranTypeDesc() { return tranTypeDesc; }
    public void setTranTypeDesc(String tranTypeDesc) { this.tranTypeDesc = tranTypeDesc; }
}
