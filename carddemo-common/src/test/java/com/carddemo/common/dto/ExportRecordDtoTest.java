package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExportRecordDtoTest {

    @Test
    void builderAndGetters() {
        ExportRecordDto dto = ExportRecordDto.builder()
                .recordType("ACCOUNT")
                .recordData("some data here")
                .build();

        assertThat(dto.getRecordType()).isEqualTo("ACCOUNT");
        assertThat(dto.getRecordData()).isEqualTo("some data here");
    }

    @Test
    void equalsAndHashCode() {
        ExportRecordDto dto1 = ExportRecordDto.builder().recordType("A").recordData("d").build();
        ExportRecordDto dto2 = ExportRecordDto.builder().recordType("A").recordData("d").build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        ExportRecordDto dto = ExportRecordDto.builder().recordType("A").build();
        assertThat(dto.toString()).contains("recordType=A");
    }

    @Test
    void setters() {
        ExportRecordDto dto = new ExportRecordDto();
        dto.setRecordType("TRAN");
        dto.setRecordData("transaction data");
        assertThat(dto.getRecordType()).isEqualTo("TRAN");
        assertThat(dto.getRecordData()).isEqualTo("transaction data");
    }
}
