package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionCategoryDtoTest {

    @Test
    void builderAndGetters() {
        TransactionCategoryDto dto = TransactionCategoryDto.builder()
                .tranTypeCd("SA")
                .tranCatCd(5001)
                .tranCatTypeDesc("Retail Purchase")
                .build();

        assertThat(dto.getTranTypeCd()).isEqualTo("SA");
        assertThat(dto.getTranCatCd()).isEqualTo(5001);
        assertThat(dto.getTranCatTypeDesc()).isEqualTo("Retail Purchase");
    }

    @Test
    void equalsAndHashCode() {
        TransactionCategoryDto dto1 = TransactionCategoryDto.builder().tranTypeCd("SA").tranCatCd(5001).build();
        TransactionCategoryDto dto2 = TransactionCategoryDto.builder().tranTypeCd("SA").tranCatCd(5001).build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        TransactionCategoryDto dto = TransactionCategoryDto.builder().tranTypeCd("SA").build();
        assertThat(dto.toString()).contains("tranTypeCd=SA");
    }

    @Test
    void setters() {
        TransactionCategoryDto dto = new TransactionCategoryDto();
        dto.setTranTypeCd("RE");
        dto.setTranCatCd(6001);
        dto.setTranCatTypeDesc("Return");
        assertThat(dto.getTranTypeCd()).isEqualTo("RE");
        assertThat(dto.getTranCatCd()).isEqualTo(6001);
    }
}
