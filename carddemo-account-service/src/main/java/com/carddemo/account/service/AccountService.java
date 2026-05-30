package com.carddemo.account.service;

import com.carddemo.account.entity.Account;
import com.carddemo.account.mapper.AccountMapper;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    public AccountDto getAccount(Long acctId) {
        log.debug("Fetching account with ID: {}", acctId);
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + acctId));
        return accountMapper.toDto(account);
    }

    @Transactional
    public AccountDto updateAccount(Long acctId, AccountDto dto) {
        log.debug("Updating account with ID: {}", acctId);
        Account existing = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + acctId));

        existing.setAcctActiveStatus(dto.getAcctActiveStatus());
        existing.setAcctCurrBal(dto.getAcctCurrBal());
        existing.setAcctCreditLimit(dto.getAcctCreditLimit());
        existing.setAcctCashCreditLimit(dto.getAcctCashCreditLimit());
        existing.setAcctOpenDate(dto.getAcctOpenDate());
        existing.setAcctExpirationDate(dto.getAcctExpirationDate());
        existing.setAcctReissueDate(dto.getAcctReissueDate());
        existing.setAcctCurrCycCredit(dto.getAcctCurrCycCredit());
        existing.setAcctCurrCycDebit(dto.getAcctCurrCycDebit());
        existing.setAcctAddrZip(dto.getAcctAddrZip());
        existing.setAcctGroupId(dto.getAcctGroupId());
        existing.setUpdatedAt(LocalDateTime.now());

        Account saved = accountRepository.save(existing);
        log.info("Account {} updated successfully", acctId);
        return accountMapper.toDto(saved);
    }

    public PagedResponse<AccountDto> listAccounts(int page, int size) {
        log.debug("Listing accounts - page: {}, size: {}", page, size);
        Page<Account> accountPage = accountRepository.findAll(PageRequest.of(page, size));
        return PagedResponse.<AccountDto>builder()
                .content(accountPage.getContent().stream().map(accountMapper::toDto).toList())
                .page(accountPage.getNumber())
                .size(accountPage.getSize())
                .totalElements(accountPage.getTotalElements())
                .totalPages(accountPage.getTotalPages())
                .build();
    }
}
