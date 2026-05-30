package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DailyTransactionDtoTest {

    @Test
    void builderAndGetters() {
        DailyTransactionDto dto = DailyTransactionDto.builder()
                .dalytranId("DTR0000000000001")
                .dalytranTypeCd("SA")
                .dalytranCatCd(5001)
                .dalytranSource("ONLINE")
                .dalytranDesc("Daily Purchase")
                .dalytranAmt(new BigDecimal("50.00"))
                .dalytranMerchantId(123456789L)
                .dalytranMerchantName("Daily Store")
                .dalytranMerchantCity("Boston")
                .dalytranMerchantZip("02101")
                .dalytranCardNum("4111111111111111")
                .dalytranOrigTs("2024-01-15T10:30:00.000000")
                .dalytranProcTs("2024-01-15T10:30:05.000000")
                .build();

        assertThat(dto.getDalytranId()).isEqualTo("DTR0000000000001");
        assertThat(dto.getDalytranTypeCd()).isEqualTo("SA");
        assertThat(dto.getDalytranCatCd()).isEqualTo(5001);
        assertThat(dto.getDalytranSource()).isEqualTo("ONLINE");
        assertThat(dto.getDalytranDesc()).isEqualTo("Daily Purchase");
        assertThat(dto.getDalytranAmt()).isEqualByComparingTo("50.00");
        assertThat(dto.getDalytranMerchantId()).isEqualTo(123456789L);
        assertThat(dto.getDalytranMerchantName()).isEqualTo("Daily Store");
        assertThat(dto.getDalytranMerchantCity()).isEqualTo("Boston");
        assertThat(dto.getDalytranMerchantZip()).isEqualTo("02101");
        assertThat(dto.getDalytranCardNum()).isEqualTo("4111111111111111");
        assertThat(dto.getDalytranOrigTs()).isEqualTo("2024-01-15T10:30:00.000000");
        assertThat(dto.getDalytranProcTs()).isEqualTo("2024-01-15T10:30:05.000000");
    }

    @Test
    void equalsAndHashCode() {
        DailyTransactionDto dto1 = DailyTransactionDto.builder().dalytranId("D1").build();
        DailyTransactionDto dto2 = DailyTransactionDto.builder().dalytranId("D1").build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        DailyTransactionDto dto = DailyTransactionDto.builder().dalytranId("D1").build();
        assertThat(dto.toString()).contains("dalytranId=D1");
    }

    @Test
    void setters() {
        DailyTransactionDto dto = new DailyTransactionDto();
        dto.setDalytranId("D1");
        dto.setDalytranAmt(BigDecimal.ONE);
        assertThat(dto.getDalytranId()).isEqualTo("D1");
        assertThat(dto.getDalytranAmt()).isEqualByComparingTo("1");
    }
}
