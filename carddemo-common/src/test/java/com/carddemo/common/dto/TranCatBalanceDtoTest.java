package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TranCatBalanceDtoTest {

    @Test
    void builderAndGetters() {
        TranCatBalanceDto dto = TranCatBalanceDto.builder()
                .trancatAcctId(12345678901L)
                .trancatTypeCd("SA")
                .trancatCd(5001)
                .tranCatBal(new BigDecimal("1500.75"))
                .build();

        assertThat(dto.getTrancatAcctId()).isEqualTo(12345678901L);
        assertThat(dto.getTrancatTypeCd()).isEqualTo("SA");
        assertThat(dto.getTrancatCd()).isEqualTo(5001);
        assertThat(dto.getTranCatBal()).isEqualByComparingTo("1500.75");
    }

    @Test
    void equalsAndHashCode() {
        TranCatBalanceDto dto1 = TranCatBalanceDto.builder().trancatAcctId(1L).trancatTypeCd("SA").build();
        TranCatBalanceDto dto2 = TranCatBalanceDto.builder().trancatAcctId(1L).trancatTypeCd("SA").build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        TranCatBalanceDto dto = TranCatBalanceDto.builder().trancatAcctId(1L).build();
        assertThat(dto.toString()).contains("trancatAcctId=1");
    }

    @Test
    void setters() {
        TranCatBalanceDto dto = new TranCatBalanceDto();
        dto.setTrancatAcctId(1L);
        dto.setTranCatBal(BigDecimal.TEN);
        assertThat(dto.getTrancatAcctId()).isEqualTo(1L);
        assertThat(dto.getTranCatBal()).isEqualByComparingTo("10");
    }
}
