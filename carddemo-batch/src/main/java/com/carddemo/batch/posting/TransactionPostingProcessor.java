package com.carddemo.batch.posting;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.DailyTransaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Validates a {@link DailyTransaction} against the 6 business rules from
 * CBTRN02C (1500-VALIDATE-TRAN) and returns a {@link PostingResult}.
 *
 * Rules:
 *   100 – XREF card not found
 *   101 – Account not found
 *   102 – Credit limit exceeded (overlimit)
 *   103 – Transaction date after account expiration
 */
public class TransactionPostingProcessor implements ItemProcessor<DailyTransaction, PostingResult> {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingProcessor.class);

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;

    public TransactionPostingProcessor(CardXrefRepository cardXrefRepository,
                                       AccountRepository accountRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public PostingResult process(DailyTransaction item) {
        // 1500-A-LOOKUP-XREF: validate card number exists in XREF
        Optional<CardXref> xrefOpt = cardXrefRepository.findById(item.getCardNum());
        if (xrefOpt.isEmpty()) {
            log.warn("Reject reason 100: invalid card number {}", item.getCardNum());
            return PostingResult.rejected(item, 100, "INVALID CARD NUMBER FOUND");
        }
        CardXref xref = xrefOpt.get();

        // 1500-B-LOOKUP-ACCT: validate account exists
        Optional<Account> acctOpt = accountRepository.findById(xref.getAcctId());
        if (acctOpt.isEmpty()) {
            log.warn("Reject reason 101: account {} not found for card {}",
                    xref.getAcctId(), item.getCardNum());
            return PostingResult.rejected(item, 101, "ACCOUNT RECORD NOT FOUND");
        }
        Account acct = acctOpt.get();

        // Rule 5 – credit limit check (CBTRN02C:393-421)
        // WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
        BigDecimal cycCredit = acct.getCurrCycCredit() != null ? acct.getCurrCycCredit() : BigDecimal.ZERO;
        BigDecimal cycDebit = acct.getCurrCycDebit() != null ? acct.getCurrCycDebit() : BigDecimal.ZERO;
        BigDecimal tempBal = cycCredit.subtract(cycDebit).add(item.getAmt());
        BigDecimal creditLimit = acct.getCreditLimit() != null ? acct.getCreditLimit() : BigDecimal.ZERO;

        if (tempBal.compareTo(creditLimit) > 0) {
            log.warn("Reject reason 102: overlimit – tempBal={} > creditLimit={}", tempBal, creditLimit);
            return PostingResult.rejected(item, 102, "OVERLIMIT TRANSACTION");
        }

        // Rule 6 – expiration check (CBTRN02C:414-420)
        // ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS(1:10)
        String expirationDate = acct.getExpirationDate();
        String tranDate = item.getOrigTs() != null && item.getOrigTs().length() >= 10
                ? item.getOrigTs().substring(0, 10)
                : item.getOrigTs();

        if (expirationDate != null && tranDate != null && expirationDate.compareTo(tranDate) < 0) {
            log.warn("Reject reason 103: expired – acct expires {}, tran date {}",
                    expirationDate, tranDate);
            return PostingResult.rejected(item, 103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
        }

        return PostingResult.accepted(item);
    }
}
