package com.carddemo.backend.account.service;

import com.carddemo.backend.account.entity.AccountEntity;
import com.carddemo.backend.account.repository.AccountRepository;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.exception.BusinessRuleViolationException;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountDto findById(Long id) {
        AccountEntity entity = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "acctId", id));
        return toDto(entity);
    }

    public Page<AccountDto> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::toDto);
    }

    public List<AccountDto> findAll() {
        return accountRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public AccountDto update(Long id, AccountDto dto) {
        AccountEntity entity = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "acctId", id));

        if (dto.getCreditLimit() != null && dto.getCreditLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("CREDIT_LIMIT_NEGATIVE",
                    "Credit limit cannot be negative");
        }
        if (dto.getCashCreditLimit() != null && dto.getCreditLimit() != null
                && dto.getCashCreditLimit().compareTo(dto.getCreditLimit()) > 0) {
            throw new BusinessRuleViolationException("CASH_LIMIT_EXCEEDS_CREDIT",
                    "Cash credit limit cannot exceed credit limit");
        }

        if (dto.getAccountStatus() != null) {
            entity.setActiveStatus(dto.getAccountStatus());
        }
        if (dto.getCurrentBalance() != null) {
            entity.setCurrBal(dto.getCurrentBalance());
        }
        if (dto.getCreditLimit() != null) {
            entity.setCreditLimit(dto.getCreditLimit());
        }
        if (dto.getCashCreditLimit() != null) {
            entity.setCashCreditLimit(dto.getCashCreditLimit());
        }
        if (dto.getOpenDate() != null) {
            entity.setOpenDate(dto.getOpenDate().toString());
        }
        if (dto.getExpirationDate() != null) {
            entity.setExpirationDate(dto.getExpirationDate().toString());
        }
        if (dto.getReissueDate() != null) {
            entity.setReissueDate(dto.getReissueDate());
        }
        if (dto.getGroupId() != null) {
            entity.setGroupId(dto.getGroupId());
        }

        AccountEntity saved = accountRepository.save(entity);
        return toDto(saved);
    }

    private AccountDto toDto(AccountEntity entity) {
        AccountDto dto = new AccountDto();
        dto.setAccountId(entity.getAcctId());
        dto.setAccountStatus(entity.getActiveStatus());
        dto.setCurrentBalance(entity.getCurrBal());
        dto.setCreditLimit(entity.getCreditLimit());
        dto.setCashCreditLimit(entity.getCashCreditLimit());
        if (entity.getOpenDate() != null && !entity.getOpenDate().isBlank()) {
            try {
                dto.setOpenDate(java.time.LocalDate.parse(entity.getOpenDate()));
            } catch (Exception e) {
                // keep null if unparseable
            }
        }
        if (entity.getExpirationDate() != null && !entity.getExpirationDate().isBlank()) {
            try {
                dto.setExpirationDate(java.time.LocalDate.parse(entity.getExpirationDate()));
            } catch (Exception e) {
                // keep null if unparseable
            }
        }
        dto.setReissueDate(entity.getReissueDate());
        dto.setGroupId(entity.getGroupId());
        dto.setCurrCycCredit(entity.getCurrCycCredit());
        dto.setCurrCycDebit(entity.getCurrCycDebit());
        dto.setAddrZip(entity.getAddrZip());
        return dto;
    }
}
