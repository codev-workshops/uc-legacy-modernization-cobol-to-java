package com.carddemo.common.dto;

public class TranCategoryDto {

    private Integer categoryCode;
    private String categoryDescription;

    public TranCategoryDto() {
    }

    public TranCategoryDto(Integer categoryCode, String categoryDescription) {
        this.categoryCode = categoryCode;
        this.categoryDescription = categoryDescription;
    }

    public Integer getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(Integer categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }
}
