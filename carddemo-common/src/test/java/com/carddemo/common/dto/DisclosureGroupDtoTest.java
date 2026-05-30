package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DisclosureGroupDtoTest {

    @Test
    void builderAndGetters() {
        DisclosureGroupDto dto = DisclosureGroupDto.builder()
                .disAcctGroupId("GRP001")
                .disTranTypeCd("SA")
                .disTranCatCd(5001)
                .disIntRate(new BigDecimal("12.50"))
                .build();

        assertThat(dto.getDisAcctGroupId()).isEqualTo("GRP001");
        assertThat(dto.getDisTranTypeCd()).isEqualTo("SA");
        assertThat(dto.getDisTranCatCd()).isEqualTo(5001);
        assertThat(dto.getDisIntRate()).isEqualByComparingTo("12.50");
    }

    @Test
    void equalsAndHashCode() {
        DisclosureGroupDto dto1 = DisclosureGroupDto.builder().disAcctGroupId("G1").build();
        DisclosureGroupDto dto2 = DisclosureGroupDto.builder().disAcctGroupId("G1").build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        DisclosureGroupDto dto = DisclosureGroupDto.builder().disAcctGroupId("G1").build();
        assertThat(dto.toString()).contains("disAcctGroupId=G1");
    }

    @Test
    void setters() {
        DisclosureGroupDto dto = new DisclosureGroupDto();
        dto.setDisAcctGroupId("G2");
        dto.setDisIntRate(BigDecimal.ONE);
        assertThat(dto.getDisAcctGroupId()).isEqualTo("G2");
        assertThat(dto.getDisIntRate()).isEqualByComparingTo("1");
    }
}
