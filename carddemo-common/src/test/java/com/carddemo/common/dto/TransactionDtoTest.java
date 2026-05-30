package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionDtoTest {

    @Test
    void builderAndGetters() {
        TransactionDto dto = TransactionDto.builder()
                .tranId("TRN0000000000001")
                .tranTypeCd("SA")
                .tranCatCd(5001)
                .tranSource("ONLINE")
                .tranDesc("Purchase at Store")
                .tranAmt(new BigDecimal("99.99"))
                .tranMerchantId(123456789L)
                .tranMerchantName("Test Store")
                .tranMerchantCity("New York")
                .tranMerchantZip("10001")
                .tranCardNum("4111111111111111")
                .tranOrigTs("2024-01-15T10:30:00.000000")
                .tranProcTs("2024-01-15T10:30:05.000000")
                .build();

        assertThat(dto.getTranId()).isEqualTo("TRN0000000000001");
        assertThat(dto.getTranTypeCd()).isEqualTo("SA");
        assertThat(dto.getTranCatCd()).isEqualTo(5001);
        assertThat(dto.getTranSource()).isEqualTo("ONLINE");
        assertThat(dto.getTranDesc()).isEqualTo("Purchase at Store");
        assertThat(dto.getTranAmt()).isEqualByComparingTo("99.99");
        assertThat(dto.getTranMerchantId()).isEqualTo(123456789L);
        assertThat(dto.getTranMerchantName()).isEqualTo("Test Store");
        assertThat(dto.getTranMerchantCity()).isEqualTo("New York");
        assertThat(dto.getTranMerchantZip()).isEqualTo("10001");
        assertThat(dto.getTranCardNum()).isEqualTo("4111111111111111");
        assertThat(dto.getTranOrigTs()).isEqualTo("2024-01-15T10:30:00.000000");
        assertThat(dto.getTranProcTs()).isEqualTo("2024-01-15T10:30:05.000000");
    }

    @Test
    void equalsAndHashCode() {
        TransactionDto dto1 = TransactionDto.builder().tranId("T1").tranTypeCd("SA").build();
        TransactionDto dto2 = TransactionDto.builder().tranId("T1").tranTypeCd("SA").build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        TransactionDto dto = TransactionDto.builder().tranId("T1").build();
        assertThat(dto.toString()).contains("tranId=T1");
    }

    @Test
    void setters() {
        TransactionDto dto = new TransactionDto();
        dto.setTranId("T1");
        dto.setTranAmt(BigDecimal.TEN);
        assertThat(dto.getTranId()).isEqualTo("T1");
        assertThat(dto.getTranAmt()).isEqualByComparingTo("10");
    }
}
