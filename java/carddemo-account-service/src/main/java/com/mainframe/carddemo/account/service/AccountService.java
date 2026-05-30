package com.mainframe.carddemo.account.service;

import com.mainframe.carddemo.account.entity.Account;
import com.mainframe.carddemo.account.repository.AccountRepository;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.BalanceUpdateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        return toDto(account);
    }

    @Transactional
    public AccountDto updateAccount(Long id, AccountDto dto) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        if (dto.getActiveStatus() != null) {
            account.setAcctActiveStatus(dto.getActiveStatus());
        }
        if (dto.getCreditLimit() != null) {
            account.setAcctCreditLimit(dto.getCreditLimit());
        }
        if (dto.getCashCreditLimit() != null) {
            account.setAcctCashCreditLimit(dto.getCashCreditLimit());
        }
        if (dto.getAddressZip() != null) {
            account.setAcctAddrZip(dto.getAddressZip());
        }
        if (dto.getGroupId() != null) {
            account.setAcctGroupId(dto.getGroupId());
        }
        Account saved = accountRepository.save(account);
        return toDto(saved);
    }

    @Transactional
    public AccountDto updateBalance(Long id, BalanceUpdateDto dto) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        if (dto.getCurrentBalance() != null) {
            account.setAcctCurrBal(dto.getCurrentBalance());
        }
        if (dto.getCurrentCycleCredit() != null) {
            account.setAcctCurrCycCredit(dto.getCurrentCycleCredit());
        }
        if (dto.getCurrentCycleDebit() != null) {
            account.setAcctCurrCycDebit(dto.getCurrentCycleDebit());
        }
        Account saved = accountRepository.save(account);
        return toDto(saved);
    }

    public static AccountDto toDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setAccountId(account.getAcctId());
        dto.setActiveStatus(account.getAcctActiveStatus());
        dto.setCurrentBalance(account.getAcctCurrBal());
        dto.setCreditLimit(account.getAcctCreditLimit());
        dto.setCashCreditLimit(account.getAcctCashCreditLimit());
        dto.setOpenDate(account.getAcctOpenDate());
        dto.setExpirationDate(account.getAcctExpirationDate());
        dto.setReissueDate(account.getAcctReissueDate());
        dto.setCurrentCycleCredit(account.getAcctCurrCycCredit());
        dto.setCurrentCycleDebit(account.getAcctCurrCycDebit());
        dto.setAddressZip(account.getAcctAddrZip());
        dto.setGroupId(account.getAcctGroupId());
        return dto;
    }
}
