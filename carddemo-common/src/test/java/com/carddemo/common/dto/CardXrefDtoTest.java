package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardXrefDtoTest {

    @Test
    void builderAndGetters() {
        CardXrefDto dto = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefCustId(123456789L)
                .xrefAcctId(12345678901L)
                .build();

        assertThat(dto.getXrefCardNum()).isEqualTo("4111111111111111");
        assertThat(dto.getXrefCustId()).isEqualTo(123456789L);
        assertThat(dto.getXrefAcctId()).isEqualTo(12345678901L);
    }

    @Test
    void equalsAndHashCode() {
        CardXrefDto dto1 = CardXrefDto.builder().xrefCardNum("4111").xrefCustId(1L).xrefAcctId(2L).build();
        CardXrefDto dto2 = CardXrefDto.builder().xrefCardNum("4111").xrefCustId(1L).xrefAcctId(2L).build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        CardXrefDto dto = CardXrefDto.builder().xrefCardNum("4111").build();
        assertThat(dto.toString()).contains("xrefCardNum=4111");
    }

    @Test
    void setters() {
        CardXrefDto dto = new CardXrefDto();
        dto.setXrefCardNum("1234");
        dto.setXrefCustId(5L);
        dto.setXrefAcctId(10L);
        assertThat(dto.getXrefCardNum()).isEqualTo("1234");
        assertThat(dto.getXrefCustId()).isEqualTo(5L);
        assertThat(dto.getXrefAcctId()).isEqualTo(10L);
    }
}
