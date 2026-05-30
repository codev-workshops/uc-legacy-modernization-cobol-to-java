package com.carddemo.batch.importer;

import com.carddemo.batch.converter.RecordConverter;
import com.carddemo.batch.export.ExportRecord;
import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.common.repository.TranCatBalanceRepository;
import com.carddemo.common.repository.TransactionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Routes imported {@link ExportRecord} items to the correct JPA repository
 * based on the record type. Batches saves by entity type within each chunk.
 */
public class ImportRecordWriter implements ItemWriter<ExportRecord> {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final TranCatBalanceRepository tranCatBalanceRepository;

    public ImportRecordWriter(CustomerRepository customerRepository,
                              AccountRepository accountRepository,
                              CardXrefRepository cardXrefRepository,
                              TransactionRepository transactionRepository,
                              CardRepository cardRepository,
                              TranCatBalanceRepository tranCatBalanceRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.tranCatBalanceRepository = tranCatBalanceRepository;
    }

    @Override
    public void write(Chunk<? extends ExportRecord> chunk) throws Exception {
        List<Customer> customers = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        List<CardXref> xrefs = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        List<Card> cards = new ArrayList<>();
        List<TranCatBalance> balances = new ArrayList<>();

        for (ExportRecord record : chunk) {
            switch (record.getRecordType()) {
                case CUSTOMER -> customers.add(RecordConverter.toCustomer(record));
                case ACCOUNT -> accounts.add(RecordConverter.toAccount(record));
                case CARD_XREF -> xrefs.add(RecordConverter.toCardXref(record));
                case TRANSACTION -> transactions.add(RecordConverter.toTransaction(record));
                case CARD -> cards.add(RecordConverter.toCard(record));
                case TRAN_CAT_BALANCE -> balances.add(RecordConverter.toTranCatBalance(record));
            }
        }

        if (!customers.isEmpty()) customerRepository.saveAll(customers);
        if (!accounts.isEmpty()) accountRepository.saveAll(accounts);
        if (!xrefs.isEmpty()) cardXrefRepository.saveAll(xrefs);
        if (!transactions.isEmpty()) transactionRepository.saveAll(transactions);
        if (!cards.isEmpty()) cardRepository.saveAll(cards);
        if (!balances.isEmpty()) tranCatBalanceRepository.saveAll(balances);
    }
}
