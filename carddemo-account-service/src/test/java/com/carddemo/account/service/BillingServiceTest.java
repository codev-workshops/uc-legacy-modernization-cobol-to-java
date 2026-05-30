package com.carddemo.account.service;

import com.carddemo.account.dto.BillingDto;
import com.carddemo.account.entity.Account;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private BillingService billingService;

    @Test
    void getAccountBilling_success() {
        Account account = Account.builder()
                .acctId(1L)
                .acctCurrBal(new BigDecimal("1000.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("1500.00"))
                .acctCurrCycCredit(new BigDecimal("200.00"))
                .acctCurrCycDebit(new BigDecimal("100.00"))
                .acctOpenDate("2024-01-01")
                .acctExpirationDate("2027-01-01")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BillingDto result = billingService.getAccountBilling(1L);

        assertThat(result.acctId()).isEqualTo(1L);
        assertThat(result.currentBalance()).isEqualByComparingTo("1000.00");
        assertThat(result.creditLimit()).isEqualByComparingTo("5000.00");
        assertThat(result.availableCredit()).isEqualByComparingTo("4000.00");
        assertThat(result.cashCreditLimit()).isEqualByComparingTo("1500.00");
        assertThat(result.cycleCredits()).isEqualByComparingTo("200.00");
        assertThat(result.cycleDebits()).isEqualByComparingTo("100.00");
        assertThat(result.statementDate()).isEqualTo("2024-01-01");
        assertThat(result.dueDate()).isEqualTo("2027-01-01");
    }

    @Test
    void getAccountBilling_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.getAccountBilling(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void getAccountBilling_nullFields() {
        Account account = Account.builder()
                .acctId(2L)
                .build();

        when(accountRepository.findById(2L)).thenReturn(Optional.of(account));

        BillingDto result = billingService.getAccountBilling(2L);

        assertThat(result.currentBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.creditLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.availableCredit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.cashCreditLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.cycleCredits()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.cycleDebits()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
