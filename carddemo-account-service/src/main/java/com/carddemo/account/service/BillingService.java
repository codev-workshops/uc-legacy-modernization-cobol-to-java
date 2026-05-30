package com.carddemo.account.service;

import com.carddemo.account.dto.BillingDto;
import com.carddemo.account.entity.Account;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final AccountRepository accountRepository;

    public BillingService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public BillingDto getAccountBilling(Long acctId) {
        log.debug("Fetching billing info for account ID: {}", acctId);
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + acctId));

        BigDecimal currentBalance = account.getAcctCurrBal() != null ? account.getAcctCurrBal() : BigDecimal.ZERO;
        BigDecimal creditLimit = account.getAcctCreditLimit() != null ? account.getAcctCreditLimit() : BigDecimal.ZERO;
        BigDecimal availableCredit = creditLimit.subtract(currentBalance);

        return new BillingDto(
                account.getAcctId(),
                currentBalance,
                creditLimit,
                account.getAcctCashCreditLimit() != null ? account.getAcctCashCreditLimit() : BigDecimal.ZERO,
                account.getAcctCurrCycCredit() != null ? account.getAcctCurrCycCredit() : BigDecimal.ZERO,
                account.getAcctCurrCycDebit() != null ? account.getAcctCurrCycDebit() : BigDecimal.ZERO,
                availableCredit,
                account.getAcctOpenDate(),
                account.getAcctExpirationDate()
        );
    }
}
