package com.carddemo.batch.processor;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.XrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Optional;

/**
 * Validates daily transactions by looking up card cross-reference and account data.
 * Replaces the main processing loop in CBTRN01C.cbl (lines 164-186):
 * 1. Look up card number in XREF file
 * 2. If XREF found, look up account by XREF's account ID
 * 3. Log success/failure; return null (skip) for invalid transactions
 */
public class TransactionValidationProcessor implements ItemProcessor<DailyTransaction, DailyTransaction> {

    private static final Logger log = LoggerFactory.getLogger(TransactionValidationProcessor.class);

    private final XrefRepository xrefRepository;
    private final AccountRepository accountRepository;

    public TransactionValidationProcessor(XrefRepository xrefRepository, AccountRepository accountRepository) {
        this.xrefRepository = xrefRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public DailyTransaction process(DailyTransaction transaction) {
        Optional<CardXrefRecord> xrefOpt = xrefRepository.findByCardNum(transaction.cardNum());

        if (xrefOpt.isEmpty()) {
            log.info("CARD NUMBER {} COULD NOT BE VERIFIED. SKIPPING TRANSACTION ID-{}",
                    transaction.cardNum(), transaction.id());
            return null;
        }

        CardXrefRecord xref = xrefOpt.get();
        log.debug("SUCCESSFUL READ OF XREF");
        log.debug("CARD NUMBER: {}", xref.cardNum());
        log.debug("ACCOUNT ID : {}", xref.acctId());
        log.debug("CUSTOMER ID: {}", xref.custId());

        Optional<AccountRecord> acctOpt = accountRepository.findByAcctId(xref.acctId());

        if (acctOpt.isEmpty()) {
            log.info("ACCOUNT {} NOT FOUND", xref.acctId());
            return null;
        }

        log.debug("SUCCESSFUL READ OF ACCOUNT FILE");
        return transaction;
    }
}
