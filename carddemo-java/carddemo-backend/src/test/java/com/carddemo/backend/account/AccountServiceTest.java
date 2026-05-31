package com.carddemo.backend.account;

import com.carddemo.backend.account.entity.AccountEntity;
import com.carddemo.backend.account.repository.AccountRepository;
import com.carddemo.backend.account.service.AccountService;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.exception.BusinessRuleViolationException;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private AccountEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleEntity = new AccountEntity();
        sampleEntity.setAcctId(1L);
        sampleEntity.setActiveStatus("Y");
        sampleEntity.setCurrBal(new BigDecimal("194.00"));
        sampleEntity.setCreditLimit(new BigDecimal("2020.00"));
        sampleEntity.setCashCreditLimit(new BigDecimal("1020.00"));
        sampleEntity.setOpenDate("2014-11-20");
        sampleEntity.setExpirationDate("2025-05-20");
        sampleEntity.setReissueDate("2025-05-20");
        sampleEntity.setCurrCycCredit(BigDecimal.ZERO);
        sampleEntity.setCurrCycDebit(BigDecimal.ZERO);
        sampleEntity.setAddrZip("A000000000");
        sampleEntity.setGroupId("");
    }

    @Test
    void findById_returnsDto() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        AccountDto result = accountService.findById(1L);

        assertEquals(1L, result.getAccountId());
        assertEquals("Y", result.getAccountStatus());
        assertEquals(new BigDecimal("194.00"), result.getCurrentBalance());
        assertEquals(new BigDecimal("2020.00"), result.getCreditLimit());
        assertEquals(new BigDecimal("1020.00"), result.getCashCreditLimit());
        assertEquals(LocalDate.of(2014, 11, 20), result.getOpenDate());
        assertEquals(LocalDate.of(2025, 5, 20), result.getExpirationDate());
        assertEquals("2025-05-20", result.getReissueDate());
        assertEquals("A000000000", result.getAddrZip());
    }

    @Test
    void findById_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.findById(999L));
    }

    @Test
    void findAll_paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccountEntity> page = new PageImpl<>(List.of(sampleEntity), pageable, 1);
        when(accountRepository.findAll(pageable)).thenReturn(page);

        Page<AccountDto> result = accountService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getAccountId());
    }

    @Test
    void findAll_list() {
        when(accountRepository.findAll()).thenReturn(List.of(sampleEntity));

        List<AccountDto> result = accountService.findAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getAccountId());
    }

    @Test
    void update_success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(accountRepository.save(any())).thenReturn(sampleEntity);

        AccountDto dto = new AccountDto();
        dto.setAccountStatus("N");
        dto.setCreditLimit(new BigDecimal("3000.00"));
        dto.setCashCreditLimit(new BigDecimal("1500.00"));
        dto.setGroupId("GRP1");

        AccountDto result = accountService.update(1L, dto);

        assertNotNull(result);
        verify(accountRepository).save(any());
    }

    @Test
    void update_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        AccountDto dto = new AccountDto();
        assertThrows(ResourceNotFoundException.class, () -> accountService.update(999L, dto));
    }

    @Test
    void update_negativeCreditLimit() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        AccountDto dto = new AccountDto();
        dto.setCreditLimit(new BigDecimal("-100.00"));

        assertThrows(BusinessRuleViolationException.class, () -> accountService.update(1L, dto));
    }

    @Test
    void update_cashLimitExceedsCreditLimit() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        AccountDto dto = new AccountDto();
        dto.setCreditLimit(new BigDecimal("1000.00"));
        dto.setCashCreditLimit(new BigDecimal("2000.00"));

        assertThrows(BusinessRuleViolationException.class, () -> accountService.update(1L, dto));
    }

    @Test
    void update_withDates() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountDto dto = new AccountDto();
        dto.setOpenDate(LocalDate.of(2020, 1, 1));
        dto.setExpirationDate(LocalDate.of(2030, 12, 31));
        dto.setReissueDate("2030-01-01");
        dto.setCurrentBalance(new BigDecimal("500.00"));

        AccountDto result = accountService.update(1L, dto);

        assertNotNull(result);
        verify(accountRepository).save(any());
    }
}
