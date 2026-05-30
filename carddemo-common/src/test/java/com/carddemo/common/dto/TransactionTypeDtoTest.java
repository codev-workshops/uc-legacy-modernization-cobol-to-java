package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTypeDtoTest {

    @Test
    void builderAndGetters() {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("SA")
                .tranTypeDesc("Sale")
                .build();

        assertThat(dto.getTranType()).isEqualTo("SA");
        assertThat(dto.getTranTypeDesc()).isEqualTo("Sale");
    }

    @Test
    void equalsAndHashCode() {
        TransactionTypeDto dto1 = TransactionTypeDto.builder().tranType("SA").build();
        TransactionTypeDto dto2 = TransactionTypeDto.builder().tranType("SA").build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        TransactionTypeDto dto = TransactionTypeDto.builder().tranType("SA").build();
        assertThat(dto.toString()).contains("tranType=SA");
    }

    @Test
    void setters() {
        TransactionTypeDto dto = new TransactionTypeDto();
        dto.setTranType("RE");
        dto.setTranTypeDesc("Return");
        assertThat(dto.getTranType()).isEqualTo("RE");
        assertThat(dto.getTranTypeDesc()).isEqualTo("Return");
    }
}
