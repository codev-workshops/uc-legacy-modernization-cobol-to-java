package com.carddemo.common.dto;

public class TranTypeDto {

    private Integer typeCode;
    private String typeDescription;

    public TranTypeDto() {
    }

    public TranTypeDto(Integer typeCode, String typeDescription) {
        this.typeCode = typeCode;
        this.typeDescription = typeDescription;
    }

    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }
}
