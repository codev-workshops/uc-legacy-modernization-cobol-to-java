package com.carddemo.batch.statement;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.common.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Enriches a {@link CardXref} record into a {@link StatementData} by looking up
 * the associated customer, account, and transactions within the statement period.
 *
 * <p>Mirrors the COBOL logic in CBSTM03A paragraphs 2000-CUSTFILE-GET,
 * 3000-ACCTFILE-GET, and 4000-TRNXFILE-GET.
 */
public class StatementProcessor implements ItemProcessor<CardXref, StatementData> {

    private static final Logger log = LoggerFactory.getLogger(StatementProcessor.class);

    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;
    private final String startDate;
    private final String endDate;

    public StatementProcessor(CustomerRepository customerRepo,
                              AccountRepository accountRepo,
                              TransactionRepository transactionRepo,
                              String startDate,
                              String endDate) {
        this.customerRepo = customerRepo;
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public StatementData process(CardXref xref) {
        Optional<Customer> custOpt = customerRepo.findById(xref.getCustId());
        if (custOpt.isEmpty()) {
            log.warn("Customer not found for custId={}, skipping card {}", xref.getCustId(), xref.getXrefCardNum());
            return null;
        }

        Optional<Account> acctOpt = accountRepo.findById(xref.getAcctId());
        if (acctOpt.isEmpty()) {
            log.warn("Account not found for acctId={}, skipping card {}", xref.getAcctId(), xref.getXrefCardNum());
            return null;
        }

        List<Transaction> allTxns = transactionRepo.findAll();
        List<Transaction> cardTxns = allTxns.stream()
                .filter(t -> xref.getXrefCardNum().equals(t.getCardNum()))
                .filter(t -> isInDateRange(t.getProcTs()))
                .sorted(Comparator.comparing(Transaction::getProcTs, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return new StatementData(custOpt.get(), acctOpt.get(), cardTxns);
    }

    boolean isInDateRange(String procTs) {
        if (procTs == null || procTs.length() < 10) {
            return false;
        }
        String dateStr = procTs.substring(0, 10);
        return dateStr.compareTo(startDate) >= 0 && dateStr.compareTo(endDate) <= 0;
    }
}
