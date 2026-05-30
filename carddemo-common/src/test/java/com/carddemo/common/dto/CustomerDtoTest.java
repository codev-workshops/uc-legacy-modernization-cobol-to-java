package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerDtoTest {

    @Test
    void builderAndGetters() {
        CustomerDto dto = CustomerDto.builder()
                .custId(123456789L)
                .custFirstName("John")
                .custMiddleName("M")
                .custLastName("Doe")
                .custAddrLine1("123 Main St")
                .custAddrLine2("Apt 4")
                .custAddrLine3("")
                .custAddrStateCd("CA")
                .custAddrCountryCd("USA")
                .custAddrZip("90210")
                .custPhoneNum1("555-1234")
                .custPhoneNum2("555-5678")
                .custSsn(123456789L)
                .custGovtIssuedId("DL12345")
                .custDob("1990-01-15")
                .custEftAccountId("EFT001")
                .custPriCardHolderInd("Y")
                .custFicoCreditScore(750)
                .build();

        assertThat(dto.getCustId()).isEqualTo(123456789L);
        assertThat(dto.getCustFirstName()).isEqualTo("John");
        assertThat(dto.getCustMiddleName()).isEqualTo("M");
        assertThat(dto.getCustLastName()).isEqualTo("Doe");
        assertThat(dto.getCustAddrLine1()).isEqualTo("123 Main St");
        assertThat(dto.getCustAddrStateCd()).isEqualTo("CA");
        assertThat(dto.getCustAddrCountryCd()).isEqualTo("USA");
        assertThat(dto.getCustAddrZip()).isEqualTo("90210");
        assertThat(dto.getCustPhoneNum1()).isEqualTo("555-1234");
        assertThat(dto.getCustSsn()).isEqualTo(123456789L);
        assertThat(dto.getCustGovtIssuedId()).isEqualTo("DL12345");
        assertThat(dto.getCustDob()).isEqualTo("1990-01-15");
        assertThat(dto.getCustEftAccountId()).isEqualTo("EFT001");
        assertThat(dto.getCustPriCardHolderInd()).isEqualTo("Y");
        assertThat(dto.getCustFicoCreditScore()).isEqualTo(750);
    }

    @Test
    void equalsAndHashCode() {
        CustomerDto dto1 = CustomerDto.builder().custId(1L).custFirstName("John").build();
        CustomerDto dto2 = CustomerDto.builder().custId(1L).custFirstName("John").build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        CustomerDto dto = CustomerDto.builder().custId(1L).build();
        assertThat(dto.toString()).contains("custId=1");
    }

    @Test
    void setters() {
        CustomerDto dto = new CustomerDto();
        dto.setCustId(1L);
        dto.setCustFirstName("Jane");
        assertThat(dto.getCustId()).isEqualTo(1L);
        assertThat(dto.getCustFirstName()).isEqualTo("Jane");
    }
}
