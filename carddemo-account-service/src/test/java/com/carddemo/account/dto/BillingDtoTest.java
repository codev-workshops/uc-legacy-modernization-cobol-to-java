package com.carddemo.account.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BillingDtoTest {

    @Test
    void recordAccessors() {
        BillingDto dto = new BillingDto(
                1L,
                new BigDecimal("1000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("4000.00"),
                "2024-01-01",
                "2027-01-01"
        );

        assertThat(dto.acctId()).isEqualTo(1L);
        assertThat(dto.currentBalance()).isEqualByComparingTo("1000.00");
        assertThat(dto.creditLimit()).isEqualByComparingTo("5000.00");
        assertThat(dto.cashCreditLimit()).isEqualByComparingTo("1500.00");
        assertThat(dto.cycleCredits()).isEqualByComparingTo("200.00");
        assertThat(dto.cycleDebits()).isEqualByComparingTo("100.00");
        assertThat(dto.availableCredit()).isEqualByComparingTo("4000.00");
        assertThat(dto.statementDate()).isEqualTo("2024-01-01");
        assertThat(dto.dueDate()).isEqualTo("2027-01-01");
    }

    @Test
    void equalsAndHashCode() {
        BillingDto dto1 = new BillingDto(1L, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN.subtract(BigDecimal.ONE), null, null);
        BillingDto dto2 = new BillingDto(1L, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN.subtract(BigDecimal.ONE), null, null);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        BillingDto dto = new BillingDto(1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null);
        assertThat(dto.toString()).contains("acctId=1");
    }
}
