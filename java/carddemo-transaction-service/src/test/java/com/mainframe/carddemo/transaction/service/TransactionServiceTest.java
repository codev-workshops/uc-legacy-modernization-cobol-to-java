package com.mainframe.carddemo.transaction.service;

import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import com.mainframe.carddemo.transaction.entity.Transaction;
import com.mainframe.carddemo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private TransactionService transactionService;

    private CardXrefDto xrefDto;
    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        xrefDto = new CardXrefDto();
        xrefDto.setCardNum("4111111111111111");
        xrefDto.setAccountId(1L);

        accountDto = new AccountDto();
        accountDto.setAccountId(1L);
        accountDto.setActiveStatus("Y");
        accountDto.setCreditLimit(new BigDecimal("10000.00"));
        accountDto.setCurrentBalance(new BigDecimal("500.00"));
        accountDto.setExpirationDate(LocalDate.of(2027, 12, 31));
    }

    @Test
    void getTransactionsByAccount_returnsPagedResults() {
        List<CardXrefDto> xrefs = List.of(xrefDto);
        when(accountServiceClient.getXrefByAccountId(1L)).thenReturn(xrefs);

        Transaction txn = new Transaction();
        txn.setTranId("0000000000000001");
        txn.setTranCardNum("4111111111111111");
        txn.setTranAmt(new BigDecimal("100.00"));
        Page<Transaction> expectedPage = new PageImpl<>(List.of(txn));
        when(transactionRepository.findByTranCardNumIn(any(), any(Pageable.class))).thenReturn(expectedPage);

        Page<Transaction> result = transactionService.getTransactionsByAccount(1L, 0, 10);

        assertEquals(1, result.getTotalElements());
        verify(accountServiceClient).getXrefByAccountId(1L);
    }

    @Test
    void getTransactionsByAccount_noCards_returnsEmpty() {
        when(accountServiceClient.getXrefByAccountId(1L)).thenReturn(Collections.emptyList());

        Page<Transaction> result = transactionService.getTransactionsByAccount(1L, 0, 10);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getTransactionById_found() {
        Transaction txn = new Transaction();
        txn.setTranId("ABC123");
        when(transactionRepository.findById("ABC123")).thenReturn(Optional.of(txn));

        Transaction result = transactionService.getTransactionById("ABC123");

        assertEquals("ABC123", result.getTranId());
    }

    @Test
    void getTransactionById_notFound() {
        when(transactionRepository.findById("MISSING")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById("MISSING"));
    }

    @Test
    void createTransaction_success() {
        when(accountServiceClient.getXrefByCardNum("4111111111111111")).thenReturn(xrefDto);
        when(accountServiceClient.getInternalAccountById(1L)).thenReturn(accountDto);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNum("4111111111111111");
        request.setTranTypeCd("01");
        request.setTranCatCd(1);
        request.setTranAmt(new BigDecimal("250.00"));
        request.setTranMerchantName("Test Merchant");
        request.setTranMerchantCity("TestCity");
        request.setTranMerchantZip("12345");
        request.setTranSource("POS TERM");
        request.setTranDesc("Test purchase");

        Transaction result = transactionService.createTransaction(request);

        assertNotNull(result.getTranId());
        assertEquals(16, result.getTranId().length());
        assertEquals("01", result.getTranTypeCd());
        assertEquals(new BigDecimal("250.00"), result.getTranAmt());
        assertNotNull(result.getTranOrigTs());
        assertNotNull(result.getTranProcTs());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_cardNotFound() {
        when(accountServiceClient.getXrefByCardNum("0000000000000000")).thenReturn(null);

        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNum("0000000000000000");

        assertThrows(ResourceNotFoundException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void createTransaction_accountNotFound() {
        when(accountServiceClient.getXrefByCardNum("4111111111111111")).thenReturn(xrefDto);
        when(accountServiceClient.getInternalAccountById(1L)).thenReturn(null);

        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNum("4111111111111111");

        assertThrows(ResourceNotFoundException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void processBillPayment_success() {
        when(accountServiceClient.getXrefByCardNum("4111111111111111")).thenReturn(xrefDto);
        when(accountServiceClient.getInternalAccountById(1L)).thenReturn(accountDto);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        BillPaymentRequest request = new BillPaymentRequest();
        request.setCardNum("4111111111111111");
        request.setAmount(new BigDecimal("100.00"));

        Transaction result = transactionService.processBillPayment(request);

        assertNotNull(result.getTranId());
        assertEquals("02", result.getTranTypeCd());
        assertEquals(new BigDecimal("-100.00"), result.getTranAmt());
        assertEquals("Bill Payment", result.getTranDesc());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processBillPayment_cardNotFound() {
        when(accountServiceClient.getXrefByCardNum("0000000000000000")).thenReturn(null);

        BillPaymentRequest request = new BillPaymentRequest();
        request.setCardNum("0000000000000000");
        request.setAmount(new BigDecimal("100.00"));

        assertThrows(ResourceNotFoundException.class, () -> transactionService.processBillPayment(request));
    }
}
