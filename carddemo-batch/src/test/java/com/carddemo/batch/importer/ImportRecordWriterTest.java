package com.carddemo.batch.importer;

import com.carddemo.batch.export.ExportRecord;
import com.carddemo.batch.export.RecordType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImportRecordWriterTest {

    @Mock private CustomerRepository customerRepo;
    @Mock private AccountRepository accountRepo;
    @Mock private CardXrefRepository cardXrefRepo;
    @Mock private TransactionRepository transactionRepo;
    @Mock private CardRepository cardRepo;
    @Mock private TranCatBalanceRepository tranCatBalanceRepo;

    private ImportRecordWriter writer;

    @BeforeEach
    void setUp() {
        writer = new ImportRecordWriter(customerRepo, accountRepo, cardXrefRepo,
                transactionRepo, cardRepo, tranCatBalanceRepo);
    }

    @Test
    void write_customerRecord_savesToCustomerRepo() throws Exception {
        ExportRecord record = new ExportRecord(RecordType.CUSTOMER, "ts", 1, "br", "rg",
                new String[]{"1000001", "John", "", "Doe", "123 Main", "", "",
                        "NY", "US", "10001", "555", "", "123456789", "DL1", "1990-01-01", "EFT1", "Y", "700"});

        writer.write(new Chunk<>(List.of(record)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Customer>> captor = ArgumentCaptor.forClass(List.class);
        verify(customerRepo).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(1000001L, captor.getValue().get(0).getCustId());
        verify(accountRepo, never()).saveAll(List.of());
    }

    @Test
    void write_accountRecord_savesToAccountRepo() throws Exception {
        ExportRecord record = new ExportRecord(RecordType.ACCOUNT, "ts", 2, "br", "rg",
                new String[]{"999", "Y", "5000.00", "10000.00", "2000.00",
                        "2020-01-01", "2025-12-31", "", "100.00", "50.00", "10001", "GRP"});

        writer.write(new Chunk<>(List.of(record)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Account>> captor = ArgumentCaptor.forClass(List.class);
        verify(accountRepo).saveAll(captor.capture());
        assertEquals(999L, captor.getValue().get(0).getAcctId());
    }

    @Test
    void write_mixedRecords_routesCorrectly() throws Exception {
        ExportRecord cust = new ExportRecord(RecordType.CUSTOMER, "ts", 1, "br", "rg",
                new String[]{"1", "F", "", "L", "", "", "", "NY", "US", "", "", "", "", "", "", "", "", ""});
        ExportRecord acct = new ExportRecord(RecordType.ACCOUNT, "ts", 2, "br", "rg",
                new String[]{"2", "Y", "100.00", "", "", "", "", "", "", "", "", ""});
        ExportRecord xref = new ExportRecord(RecordType.CARD_XREF, "ts", 3, "br", "rg",
                new String[]{"CARD1", "1", "2"});

        writer.write(new Chunk<>(List.of(cust, acct, xref)));

        verify(customerRepo).saveAll(org.mockito.ArgumentMatchers.<Customer>anyList());
        verify(accountRepo).saveAll(org.mockito.ArgumentMatchers.<Account>anyList());
        verify(cardXrefRepo).saveAll(org.mockito.ArgumentMatchers.<CardXref>anyList());
    }

    @Test
    void write_cardRecord_savesToCardRepo() throws Exception {
        ExportRecord record = new ExportRecord(RecordType.CARD, "ts", 5, "br", "rg",
                new String[]{"4111111111111111", "12345", "123", "JOHN DOE", "2025-12-31", "Y"});

        writer.write(new Chunk<>(List.of(record)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Card>> captor = ArgumentCaptor.forClass(List.class);
        verify(cardRepo).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals("4111111111111111", captor.getValue().get(0).getCardNum());
    }

    @Test
    void write_tranCatBalanceRecord_savesToTranCatBalanceRepo() throws Exception {
        ExportRecord record = new ExportRecord(RecordType.TRAN_CAT_BALANCE, "ts", 6, "br", "rg",
                new String[]{"12345", "SA", "5001", "1500.75"});

        writer.write(new Chunk<>(List.of(record)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TranCatBalance>> captor = ArgumentCaptor.forClass(List.class);
        verify(tranCatBalanceRepo).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(12345L, captor.getValue().get(0).getAcctId());
    }

    @Test
    void write_emptyChunk_doesNothing() throws Exception {
        writer.write(new Chunk<>(List.of()));

        verify(customerRepo, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        verify(accountRepo, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        verify(cardXrefRepo, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        verify(transactionRepo, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        verify(cardRepo, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        verify(tranCatBalanceRepo, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }
}
