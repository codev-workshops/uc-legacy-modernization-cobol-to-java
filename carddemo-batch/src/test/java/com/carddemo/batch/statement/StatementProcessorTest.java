package com.carddemo.batch.statement;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.common.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementProcessorTest {

    @Mock
    private CustomerRepository customerRepo;
    @Mock
    private AccountRepository accountRepo;
    @Mock
    private TransactionRepository transactionRepo;

    private StatementProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new StatementProcessor(customerRepo, accountRepo, transactionRepo,
                "2024-01-01", "2024-01-31");
    }

    @Test
    void process_returnsStatementData() {
        CardXref xref = new CardXref();
        xref.setXrefCardNum("1111111111111111");
        xref.setCustId(100L);
        xref.setAcctId(200L);

        Customer cust = new Customer();
        cust.setCustId(100L);
        cust.setFirstName("Test");
        cust.setLastName("User");
        when(customerRepo.findById(100L)).thenReturn(Optional.of(cust));

        Account acct = new Account();
        acct.setAcctId(200L);
        acct.setCurrBal(new BigDecimal("1000.00"));
        when(accountRepo.findById(200L)).thenReturn(Optional.of(acct));

        Transaction tx = new Transaction();
        tx.setTranId("TXN001");
        tx.setCardNum("1111111111111111");
        tx.setProcTs("2024-01-15-10.00.00.000000");
        tx.setAmt(new BigDecimal("50.00"));

        Transaction txOther = new Transaction();
        txOther.setTranId("TXN002");
        txOther.setCardNum("2222222222222222");
        txOther.setProcTs("2024-01-15-10.00.00.000000");
        txOther.setAmt(new BigDecimal("100.00"));

        when(transactionRepo.findAll()).thenReturn(List.of(tx, txOther));

        StatementData result = processor.process(xref);

        assertNotNull(result);
        assertEquals(cust, result.getCustomer());
        assertEquals(acct, result.getAccount());
        assertEquals(1, result.getTransactions().size());
        assertEquals("TXN001", result.getTransactions().get(0).getTranId());
    }

    @Test
    void process_returnsNull_whenCustomerNotFound() {
        CardXref xref = new CardXref();
        xref.setXrefCardNum("1111111111111111");
        xref.setCustId(999L);
        xref.setAcctId(200L);

        when(customerRepo.findById(999L)).thenReturn(Optional.empty());

        assertNull(processor.process(xref));
    }

    @Test
    void process_returnsNull_whenAccountNotFound() {
        CardXref xref = new CardXref();
        xref.setXrefCardNum("1111111111111111");
        xref.setCustId(100L);
        xref.setAcctId(999L);

        Customer cust = new Customer();
        cust.setCustId(100L);
        when(customerRepo.findById(100L)).thenReturn(Optional.of(cust));
        when(accountRepo.findById(999L)).thenReturn(Optional.empty());

        assertNull(processor.process(xref));
    }

    @Test
    void process_filtersOutOfRangeTransactions() {
        CardXref xref = new CardXref();
        xref.setXrefCardNum("1111111111111111");
        xref.setCustId(100L);
        xref.setAcctId(200L);

        when(customerRepo.findById(100L)).thenReturn(Optional.of(new Customer()));
        when(accountRepo.findById(200L)).thenReturn(Optional.of(new Account()));

        Transaction txInRange = new Transaction();
        txInRange.setTranId("TXN001");
        txInRange.setCardNum("1111111111111111");
        txInRange.setProcTs("2024-01-15-10.00.00.000000");

        Transaction txOutRange = new Transaction();
        txOutRange.setTranId("TXN002");
        txOutRange.setCardNum("1111111111111111");
        txOutRange.setProcTs("2024-02-15-10.00.00.000000");

        when(transactionRepo.findAll()).thenReturn(List.of(txInRange, txOutRange));

        StatementData result = processor.process(xref);
        assertNotNull(result);
        assertEquals(1, result.getTransactions().size());
        assertEquals("TXN001", result.getTransactions().get(0).getTranId());
    }

    @Test
    void isInDateRange_validDates() {
        assertTrue(processor.isInDateRange("2024-01-01-00.00.00.000000"));
        assertTrue(processor.isInDateRange("2024-01-31-23.59.59.999999"));
        assertTrue(processor.isInDateRange("2024-01-15-12.00.00.000000"));
        assertFalse(processor.isInDateRange("2023-12-31-23.59.59.999999"));
        assertFalse(processor.isInDateRange("2024-02-01-00.00.00.000000"));
    }

    @Test
    void isInDateRange_nullOrShortString() {
        assertFalse(processor.isInDateRange(null));
        assertFalse(processor.isInDateRange("short"));
    }
}
