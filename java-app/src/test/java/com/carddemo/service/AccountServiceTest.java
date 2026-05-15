package com.carddemo.service;

import com.carddemo.model.Account;
import com.carddemo.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrentBalance(new BigDecimal("1940.00"));
        testAccount.setCreditLimit(new BigDecimal("20200.00"));
        testAccount.setCashCreditLimit(new BigDecimal("10200.00"));
        testAccount.setOpenDate(LocalDate.of(2014, 11, 20));
    }

    @Test
    void findAll_returnsAllAccounts() {
        when(accountRepository.findAll()).thenReturn(List.of(testAccount));
        List<Account> result = accountService.findAll();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getAccountId());
    }

    @Test
    void findById_existingAccount_returnsAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        Optional<Account> result = accountService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("Y", result.get().getActiveStatus());
    }

    @Test
    void findById_nonExistingAccount_returnsEmpty() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<Account> result = accountService.findById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    void findByActiveStatus_returnsMatchingAccounts() {
        when(accountRepository.findByActiveStatus("Y")).thenReturn(List.of(testAccount));
        List<Account> result = accountService.findByActiveStatus("Y");
        assertEquals(1, result.size());
    }

    @Test
    void save_persistsAccount() {
        when(accountRepository.save(testAccount)).thenReturn(testAccount);
        Account result = accountService.save(testAccount);
        assertEquals(1L, result.getAccountId());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void deleteById_deletesAccount() {
        accountService.deleteById(1L);
        verify(accountRepository).deleteById(1L);
    }
}
