package com.carddemo.common.service;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Shared posting-validation logic extracted from CBTRN02C (1500-VALIDATE-TRAN).
 * Reusable by both the batch {@code TransactionPostingProcessor} and the
 * online {@code TransactionService}.
 */
public class TransactionValidationService {

    private static final Logger log = LoggerFactory.getLogger(TransactionValidationService.class);

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;

    public TransactionValidationService(CardXrefRepository cardXrefRepository,
                                        AccountRepository accountRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Validates a transaction against the CBTRN02C business rules.
     *
     * @param cardNum         card number (16-char)
     * @param amount          transaction amount
     * @param transactionDate date portion of the transaction timestamp (yyyy-MM-dd)
     * @return validation result containing the resolved account ID on success,
     *         or a rejection reason code and description on failure
     */
    public ValidationResult validate(String cardNum, BigDecimal amount, String transactionDate) {
        // Rule 100 – XREF card not found
        Optional<CardXref> xrefOpt = cardXrefRepository.findById(cardNum);
        if (xrefOpt.isEmpty()) {
            log.warn("Reject reason 100: invalid card number {}", cardNum);
            return ValidationResult.rejected(100, "INVALID CARD NUMBER FOUND");
        }
        CardXref xref = xrefOpt.get();

        // Rule 101 – Account not found
        Optional<Account> acctOpt = accountRepository.findById(xref.getAcctId());
        if (acctOpt.isEmpty()) {
            log.warn("Reject reason 101: account {} not found for card {}",
                    xref.getAcctId(), cardNum);
            return ValidationResult.rejected(101, "ACCOUNT RECORD NOT FOUND");
        }
        Account acct = acctOpt.get();

        // Rule 102 – credit limit check (CBTRN02C:393-421)
        BigDecimal cycCredit = acct.getCurrCycCredit() != null ? acct.getCurrCycCredit() : BigDecimal.ZERO;
        BigDecimal cycDebit = acct.getCurrCycDebit() != null ? acct.getCurrCycDebit() : BigDecimal.ZERO;
        BigDecimal tempBal = cycCredit.subtract(cycDebit).add(amount);
        BigDecimal creditLimit = acct.getCreditLimit() != null ? acct.getCreditLimit() : BigDecimal.ZERO;

        if (tempBal.compareTo(creditLimit) > 0) {
            log.warn("Reject reason 102: overlimit – tempBal={} > creditLimit={}", tempBal, creditLimit);
            return ValidationResult.rejected(102, "OVERLIMIT TRANSACTION");
        }

        // Rule 103 – expiration check (CBTRN02C:414-420)
        String expirationDate = acct.getExpirationDate();
        if (expirationDate != null && transactionDate != null
                && expirationDate.compareTo(transactionDate) < 0) {
            log.warn("Reject reason 103: expired – acct expires {}, tran date {}",
                    expirationDate, transactionDate);
            return ValidationResult.rejected(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
        }

        return ValidationResult.accepted(xref.getAcctId());
    }
}
