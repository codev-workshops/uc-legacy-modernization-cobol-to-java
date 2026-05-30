package com.carddemo.account.service;

import com.carddemo.account.entity.Account;
import com.carddemo.account.mapper.AccountMapper;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .acctId(1L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("1000.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("1000.00"))
                .acctOpenDate("2024-01-01")
                .acctExpirationDate("2027-01-01")
                .acctCurrCycCredit(new BigDecimal("200.00"))
                .acctCurrCycDebit(new BigDecimal("100.00"))
                .acctAddrZip("10001")
                .acctGroupId("GRP001")
                .build();

        accountDto = AccountDto.builder()
                .acctId(1L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("1000.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("1000.00"))
                .acctOpenDate("2024-01-01")
                .acctExpirationDate("2027-01-01")
                .acctCurrCycCredit(new BigDecimal("200.00"))
                .acctCurrCycDebit(new BigDecimal("100.00"))
                .acctAddrZip("10001")
                .acctGroupId("GRP001")
                .build();
    }

    @Test
    void getAccount_found() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        AccountDto result = accountService.getAccount(1L);

        assertThat(result).isEqualTo(accountDto);
        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccount_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void updateAccount_success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toDto(any(Account.class))).thenReturn(accountDto);

        AccountDto result = accountService.updateAccount(1L, accountDto);

        assertThat(result).isEqualTo(accountDto);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateAccount(999L, accountDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void listAccounts() {
        Page<Account> page = new PageImpl<>(List.of(account), PageRequest.of(0, 20), 1);
        when(accountRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        PagedResponse<AccountDto> result = accountService.listAccounts(0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPage()).isZero();
    }
}
