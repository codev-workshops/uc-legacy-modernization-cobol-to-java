package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionReportDtoTest {

    @Test
    void builderAndGetters() {
        TransactionReportDto dto = TransactionReportDto.builder()
                .tranReportTransId("TRN001")
                .tranReportAccountId("ACCT001")
                .tranReportTypeCd("SA")
                .tranReportTypeDesc("Sale")
                .tranReportCatCd(5001)
                .tranReportCatDesc("Retail")
                .tranReportSource("ONLINE")
                .tranReportAmt(new BigDecimal("250.00"))
                .build();

        assertThat(dto.getTranReportTransId()).isEqualTo("TRN001");
        assertThat(dto.getTranReportAccountId()).isEqualTo("ACCT001");
        assertThat(dto.getTranReportTypeCd()).isEqualTo("SA");
        assertThat(dto.getTranReportTypeDesc()).isEqualTo("Sale");
        assertThat(dto.getTranReportCatCd()).isEqualTo(5001);
        assertThat(dto.getTranReportCatDesc()).isEqualTo("Retail");
        assertThat(dto.getTranReportSource()).isEqualTo("ONLINE");
        assertThat(dto.getTranReportAmt()).isEqualByComparingTo("250.00");
    }

    @Test
    void equalsAndHashCode() {
        TransactionReportDto dto1 = TransactionReportDto.builder().tranReportTransId("T1").build();
        TransactionReportDto dto2 = TransactionReportDto.builder().tranReportTransId("T1").build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        TransactionReportDto dto = TransactionReportDto.builder().tranReportTransId("T1").build();
        assertThat(dto.toString()).contains("tranReportTransId=T1");
    }

    @Test
    void setters() {
        TransactionReportDto dto = new TransactionReportDto();
        dto.setTranReportTransId("T2");
        dto.setTranReportAmt(BigDecimal.TEN);
        assertThat(dto.getTranReportTransId()).isEqualTo("T2");
        assertThat(dto.getTranReportAmt()).isEqualByComparingTo("10");
    }
}
