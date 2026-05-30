package com.carddemo.online.service;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TransactionRepository;
import com.carddemo.online.dto.BillPaymentRequest;
import com.carddemo.online.dto.BillPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillPaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private BillPaymentService billPaymentService;

    private Account activeAccount;
    private CardXref cardXref;

    @BeforeEach
    void setUp() {
        activeAccount = new Account();
        activeAccount.setAcctId(10001L);
        activeAccount.setActiveStatus("Y");
        activeAccount.setCurrBal(new BigDecimal("1000.00"));
        activeAccount.setCurrCycCredit(BigDecimal.ZERO);
        activeAccount.setCurrCycDebit(BigDecimal.ZERO);

        cardXref = new CardXref();
        cardXref.setXrefCardNum("4111111111111111");
        cardXref.setAcctId(10001L);
        cardXref.setCustId(1L);
    }

    @Test
    void processPayment_success() {
        when(accountRepository.findById(10001L)).thenReturn(Optional.of(activeAccount));
        when(cardXrefRepository.findAll()).thenReturn(List.of(cardXref));
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("500.00"), "ONLINE");

        BillPaymentResponse response = billPaymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(10001L, response.getAccountId());
        assertEquals(new BigDecimal("500.00"), response.getPaymentAmount());
        assertEquals(new BigDecimal("500.00"), response.getNewBalance());
        assertEquals("0000000000000001", response.getTransactionId());

        ArgumentCaptor<Transaction> txnCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txnCaptor.capture());
        Transaction savedTxn = txnCaptor.getValue();
        assertEquals("02", savedTxn.getTypeCd());
        assertEquals(2, savedTxn.getCatCd());
        assertEquals("BILL PAYMENT - ONLINE", savedTxn.getDesc());
        assertEquals(new BigDecimal("500.00"), savedTxn.getAmt());
        assertEquals("4111111111111111", savedTxn.getCardNum());

        ArgumentCaptor<Account> acctCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(acctCaptor.capture());
        Account savedAcct = acctCaptor.getValue();
        assertEquals(new BigDecimal("500.00"), savedAcct.getCurrBal());
        assertEquals(new BigDecimal("500.00"), savedAcct.getCurrCycCredit());
    }

    @Test
    void processPayment_defaultSource() {
        when(accountRepository.findById(10001L)).thenReturn(Optional.of(activeAccount));
        when(cardXrefRepository.findAll()).thenReturn(List.of(cardXref));
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        BillPaymentRequest request = new BillPaymentRequest(10001L, new BigDecimal("200.00"), null);

        BillPaymentResponse response = billPaymentService.processPayment(request);
        assertNotNull(response);

        ArgumentCaptor<Transaction> txnCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txnCaptor.capture());
        assertEquals("POS TERM", txnCaptor.getValue().getSource());
    }

    @Test
    void processPayment_accountNotFound() {
        when(accountRepository.findById(99999L)).thenReturn(Optional.empty());

        BillPaymentRequest request = new BillPaymentRequest(
                99999L, new BigDecimal("500.00"), "ONLINE");

        assertThrows(BillPaymentService.AccountNotFoundException.class,
                () -> billPaymentService.processPayment(request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processPayment_accountNotActive() {
        Account inactive = new Account();
        inactive.setAcctId(10002L);
        inactive.setActiveStatus("N");
        inactive.setCurrBal(new BigDecimal("1000.00"));
        when(accountRepository.findById(10002L)).thenReturn(Optional.of(inactive));

        BillPaymentRequest request = new BillPaymentRequest(
                10002L, new BigDecimal("500.00"), "ONLINE");

        assertThrows(BillPaymentService.AccountNotActiveException.class,
                () -> billPaymentService.processPayment(request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processPayment_noXrefUsesDefault() {
        when(accountRepository.findById(10001L)).thenReturn(Optional.of(activeAccount));
        when(cardXrefRepository.findAll()).thenReturn(Collections.emptyList());
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("100.00"), "ONLINE");

        BillPaymentResponse response = billPaymentService.processPayment(request);
        assertNotNull(response);

        ArgumentCaptor<Transaction> txnCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txnCaptor.capture());
        assertEquals("0000000000000000", txnCaptor.getValue().getCardNum());
    }

    @Test
    void processPayment_incrementsExistingTranId() {
        when(accountRepository.findById(10001L)).thenReturn(Optional.of(activeAccount));
        when(cardXrefRepository.findAll()).thenReturn(List.of(cardXref));

        Transaction existing = new Transaction();
        existing.setTranId("0000000000000042");
        when(transactionRepository.findAll()).thenReturn(List.of(existing));

        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("300.00"), "ONLINE");

        BillPaymentResponse response = billPaymentService.processPayment(request);
        assertEquals("0000000000000043", response.getTransactionId());
    }

    @Test
    void processPayment_fullBalancePayment() {
        when(accountRepository.findById(10001L)).thenReturn(Optional.of(activeAccount));
        when(cardXrefRepository.findAll()).thenReturn(List.of(cardXref));
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("1000.00"), "ONLINE");

        BillPaymentResponse response = billPaymentService.processPayment(request);
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getNewBalance()));
    }

    @Test
    void processPayment_nullCurrBalTreatedAsZero() {
        activeAccount.setCurrBal(null);
        activeAccount.setCurrCycCredit(null);
        when(accountRepository.findById(10001L)).thenReturn(Optional.of(activeAccount));
        when(cardXrefRepository.findAll()).thenReturn(List.of(cardXref));
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("100.00"), "ONLINE");

        BillPaymentResponse response = billPaymentService.processPayment(request);
        assertEquals(new BigDecimal("-100.00"), response.getNewBalance());
    }
}
