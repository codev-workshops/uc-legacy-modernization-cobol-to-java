package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountDtoTest {

    @Test
    void builderAndGetters() {
        AccountDto dto = AccountDto.builder()
                .acctId(12345678901L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("1000.50"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("2000.00"))
                .acctOpenDate("2020-01-15")
                .acctExpirationDate("2025-01-15")
                .acctReissueDate("2023-01-15")
                .acctCurrCycCredit(new BigDecimal("500.00"))
                .acctCurrCycDebit(new BigDecimal("200.00"))
                .acctAddrZip("90210")
                .acctGroupId("GRP001")
                .build();

        assertThat(dto.getAcctId()).isEqualTo(12345678901L);
        assertThat(dto.getAcctActiveStatus()).isEqualTo("Y");
        assertThat(dto.getAcctCurrBal()).isEqualByComparingTo("1000.50");
        assertThat(dto.getAcctCreditLimit()).isEqualByComparingTo("5000.00");
        assertThat(dto.getAcctCashCreditLimit()).isEqualByComparingTo("2000.00");
        assertThat(dto.getAcctOpenDate()).isEqualTo("2020-01-15");
        assertThat(dto.getAcctExpirationDate()).isEqualTo("2025-01-15");
        assertThat(dto.getAcctReissueDate()).isEqualTo("2023-01-15");
        assertThat(dto.getAcctCurrCycCredit()).isEqualByComparingTo("500.00");
        assertThat(dto.getAcctCurrCycDebit()).isEqualByComparingTo("200.00");
        assertThat(dto.getAcctAddrZip()).isEqualTo("90210");
        assertThat(dto.getAcctGroupId()).isEqualTo("GRP001");
    }

    @Test
    void setters() {
        AccountDto dto = new AccountDto();
        dto.setAcctId(1L);
        dto.setAcctActiveStatus("N");
        assertThat(dto.getAcctId()).isEqualTo(1L);
        assertThat(dto.getAcctActiveStatus()).isEqualTo("N");
    }

    @Test
    void equalsAndHashCode() {
        AccountDto dto1 = AccountDto.builder().acctId(1L).acctActiveStatus("Y").build();
        AccountDto dto2 = AccountDto.builder().acctId(1L).acctActiveStatus("Y").build();
        AccountDto dto3 = AccountDto.builder().acctId(2L).acctActiveStatus("N").build();

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1).isNotEqualTo(dto3);
    }

    @Test
    void toStringContainsFields() {
        AccountDto dto = AccountDto.builder().acctId(1L).build();
        assertThat(dto.toString()).contains("acctId=1");
    }

    @Test
    void noArgsConstructor() {
        AccountDto dto = new AccountDto();
        assertThat(dto.getAcctId()).isNull();
        assertThat(dto.getAcctActiveStatus()).isNull();
    }

    @Test
    void allArgsConstructor() {
        AccountDto dto = new AccountDto(1L, "Y", BigDecimal.TEN, BigDecimal.ONE,
                BigDecimal.ZERO, "2020-01-01", "2025-01-01", "2023-01-01",
                BigDecimal.TEN, BigDecimal.ONE, "12345", "GRP1");
        assertThat(dto.getAcctId()).isEqualTo(1L);
        assertThat(dto.getAcctGroupId()).isEqualTo("GRP1");
    }
}
