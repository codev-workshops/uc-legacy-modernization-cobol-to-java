package com.carddemo.common.entity;

import java.io.Serializable;
import java.util.Objects;

public class TranCategoryId implements Serializable {

    private String typeCd;
    private Integer catCd;

    public TranCategoryId() {}

    public TranCategoryId(String typeCd, Integer catCd) {
        this.typeCd = typeCd;
        this.catCd = catCd;
    }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }
    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranCategoryId that)) return false;
        return Objects.equals(typeCd, that.typeCd)
                && Objects.equals(catCd, that.catCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeCd, catCd);
    }
}
