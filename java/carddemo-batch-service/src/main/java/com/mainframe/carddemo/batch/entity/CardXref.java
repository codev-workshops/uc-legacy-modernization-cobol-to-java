package com.mainframe.carddemo.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16, nullable = false)
    private String xrefCardNum;

    @Column(name = "xref_cust_id", nullable = false)
    private Long xrefCustId;

    @Column(name = "xref_acct_id", nullable = false)
    private Long xrefAcctId;

    public CardXref() {}

    public String getXrefCardNum() { return xrefCardNum; }
    public void setXrefCardNum(String v) { this.xrefCardNum = v; }

    public Long getXrefCustId() { return xrefCustId; }
    public void setXrefCustId(Long v) { this.xrefCustId = v; }

    public Long getXrefAcctId() { return xrefAcctId; }
    public void setXrefAcctId(Long v) { this.xrefAcctId = v; }
}
