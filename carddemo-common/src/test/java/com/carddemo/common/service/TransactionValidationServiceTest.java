package com.carddemo.common.service;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionValidationServiceTest {

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private AccountRepository accountRepository;

    private TransactionValidationService service;

    @BeforeEach
    void setUp() {
        service = new TransactionValidationService(cardXrefRepository, accountRepository);
    }

    private CardXref xref() {
        CardXref x = new CardXref();
        x.setXrefCardNum("4111111111111111");
        x.setCustId(1L);
        x.setAcctId(100L);
        return x;
    }

    private Account account() {
        Account a = new Account();
        a.setAcctId(100L);
        a.setCreditLimit(new BigDecimal("5000.00"));
        a.setCurrCycCredit(new BigDecimal("500.00"));
        a.setCurrCycDebit(new BigDecimal("200.00"));
        a.setExpirationDate("2028-12-31");
        return a;
    }

    @Test
    void validate_accepted() {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref()));
        when(accountRepository.findById(100L)).thenReturn(Optional.of(account()));

        ValidationResult result = service.validate("4111111111111111",
                new BigDecimal("100.00"), "2026-06-01");

        assertTrue(result.isAccepted());
        assertEquals(100L, result.getAccountId());
    }

    @Test
    void validate_rejects100_invalidCard() {
        when(cardXrefRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        ValidationResult result = service.validate("9999999999999999",
                new BigDecimal("10.00"), "2026-06-01");

        assertFalse(result.isAccepted());
        assertEquals(100, result.getReasonCode());
    }

    @Test
    void validate_rejects101_accountNotFound() {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref()));
        when(accountRepository.findById(100L)).thenReturn(Optional.empty());

        ValidationResult result = service.validate("4111111111111111",
                new BigDecimal("10.00"), "2026-06-01");

        assertFalse(result.isAccepted());
        assertEquals(101, result.getReasonCode());
    }

    @Test
    void validate_rejects102_overlimit() {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref()));
        Account acct = account();
        acct.setCreditLimit(new BigDecimal("100.00"));
        acct.setCurrCycCredit(new BigDecimal("90.00"));
        acct.setCurrCycDebit(new BigDecimal("0.00"));
        when(accountRepository.findById(100L)).thenReturn(Optional.of(acct));

        ValidationResult result = service.validate("4111111111111111",
                new BigDecimal("50.00"), "2026-06-01");

        assertFalse(result.isAccepted());
        assertEquals(102, result.getReasonCode());
        assertEquals("OVERLIMIT TRANSACTION", result.getReasonDescription());
    }

    @Test
    void validate_rejects103_expired() {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref()));
        Account acct = account();
        acct.setExpirationDate("2020-01-01");
        when(accountRepository.findById(100L)).thenReturn(Optional.of(acct));

        ValidationResult result = service.validate("4111111111111111",
                new BigDecimal("10.00"), "2026-06-01");

        assertFalse(result.isAccepted());
        assertEquals(103, result.getReasonCode());
    }

    @Test
    void validate_nullCreditFields_treatedAsZero() {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref()));
        Account acct = account();
        acct.setCurrCycCredit(null);
        acct.setCurrCycDebit(null);
        acct.setCreditLimit(new BigDecimal("100.00"));
        when(accountRepository.findById(100L)).thenReturn(Optional.of(acct));

        ValidationResult result = service.validate("4111111111111111",
                new BigDecimal("50.00"), "2026-06-01");

        assertTrue(result.isAccepted());
    }

    @Test
    void validate_nullExpirationDate_accepted() {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref()));
        Account acct = account();
        acct.setExpirationDate(null);
        when(accountRepository.findById(100L)).thenReturn(Optional.of(acct));

        ValidationResult result = service.validate("4111111111111111",
                new BigDecimal("10.00"), "2026-06-01");

        assertTrue(result.isAccepted());
    }

    @Test
    void validate_exactCreditLimit_accepted() {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref()));
        Account acct = account();
        acct.setCreditLimit(new BigDecimal("350.00"));
        acct.setCurrCycCredit(new BigDecimal("500.00"));
        acct.setCurrCycDebit(new BigDecimal("200.00"));
        when(accountRepository.findById(100L)).thenReturn(Optional.of(acct));

        // tempBal = 500 - 200 + 50 = 350 == 350 (limit) => accepted
        ValidationResult result = service.validate("4111111111111111",
                new BigDecimal("50.00"), "2026-06-01");

        assertTrue(result.isAccepted());
    }
}
