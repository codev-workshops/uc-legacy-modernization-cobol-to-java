package com.carddemo.online.service;

import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TransactionRepository;
import com.carddemo.common.service.TransactionValidationService;
import com.carddemo.common.service.ValidationResult;
import com.carddemo.online.dto.TransactionRequest;
import com.carddemo.online.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private TransactionValidationService validationService;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository, cardXrefRepository, validationService);
    }

    private Transaction sampleTransaction() {
        Transaction txn = new Transaction();
        txn.setTranId("TXN0000000000001");
        txn.setTypeCd("SA");
        txn.setCatCd(5001);
        txn.setSource("ONLINE");
        txn.setDesc("Test purchase");
        txn.setAmt(new BigDecimal("25.50"));
        txn.setCardNum("4111111111111111");
        txn.setOrigTs("2026-01-15-10.30.00.000000");
        txn.setProcTs("2026-01-15-10.30.00.000000");
        return txn;
    }

    @Test
    void listTransactions_noFilters_returnsAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> page = new PageImpl<>(List.of(sampleTransaction()), pageable, 1);
        when(transactionRepository.findFiltered(null, null, null, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.listTransactions(
                null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("TXN0000000000001", result.getContent().get(0).getTranId());
    }

    @Test
    void listTransactions_byCardNum_filtersCorrectly() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> page = new PageImpl<>(List.of(sampleTransaction()), pageable, 1);
        when(transactionRepository.findFiltered("4111111111111111", null, null, pageable))
                .thenReturn(page);

        Page<TransactionResponse> result = transactionService.listTransactions(
                null, "4111111111111111", null, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listTransactions_byAccountId_looksUpCards() {
        Pageable pageable = PageRequest.of(0, 20);
        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setAcctId(100L);
        when(cardXrefRepository.findByAcctId(100L)).thenReturn(List.of(xref));

        Page<Transaction> page = new PageImpl<>(List.of(sampleTransaction()), pageable, 1);
        when(transactionRepository.findByCardNumsFiltered(
                List.of("4111111111111111"), null, null, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.listTransactions(
                100L, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listTransactions_byAccountId_noCards_returnsEmpty() {
        Pageable pageable = PageRequest.of(0, 20);
        when(cardXrefRepository.findByAcctId(999L)).thenReturn(List.of());

        Page<TransactionResponse> result = transactionService.listTransactions(
                999L, null, null, null, pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getTransaction_found_returnsResponse() {
        when(transactionRepository.findById("TXN0000000000001"))
                .thenReturn(Optional.of(sampleTransaction()));

        TransactionResponse response = transactionService.getTransaction("TXN0000000000001");

        assertEquals("TXN0000000000001", response.getTranId());
        assertEquals(new BigDecimal("25.50"), response.getAmt());
    }

    @Test
    void getTransaction_notFound_throws() {
        when(transactionRepository.findById("MISSING")).thenReturn(Optional.empty());

        assertThrows(TransactionService.TransactionNotFoundException.class,
                () -> transactionService.getTransaction("MISSING"));
    }

    @Test
    void addTransaction_valid_savesAndReturns() {
        when(validationService.validate(eq("4111111111111111"), any(BigDecimal.class), anyString()))
                .thenReturn(ValidationResult.accepted(100L));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("25.50"));
        request.setTypeCd("SA");
        request.setDescription("Test purchase");

        TransactionResponse response = transactionService.addTransaction(request);

        assertNotNull(response.getTranId());
        assertEquals("4111111111111111", response.getCardNum());
        assertEquals(new BigDecimal("25.50"), response.getAmt());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void addTransaction_overlimit_throws102() {
        when(validationService.validate(eq("4111111111111111"), any(BigDecimal.class), anyString()))
                .thenReturn(ValidationResult.rejected(102, "OVERLIMIT TRANSACTION"));

        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("99999.99"));

        TransactionService.TransactionRejectedException ex = assertThrows(
                TransactionService.TransactionRejectedException.class,
                () -> transactionService.addTransaction(request));

        assertEquals(102, ex.getReasonCode());
        assertEquals("OVERLIMIT TRANSACTION", ex.getMessage());
    }

    @Test
    void addTransaction_expiredCard_throws103() {
        when(validationService.validate(eq("4111111111111111"), any(BigDecimal.class), anyString()))
                .thenReturn(ValidationResult.rejected(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"));

        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("10.00"));

        TransactionService.TransactionRejectedException ex = assertThrows(
                TransactionService.TransactionRejectedException.class,
                () -> transactionService.addTransaction(request));

        assertEquals(103, ex.getReasonCode());
    }

    @Test
    void addTransaction_invalidCard_throws100() {
        when(validationService.validate(eq("9999999999999999"), any(BigDecimal.class), anyString()))
                .thenReturn(ValidationResult.rejected(100, "INVALID CARD NUMBER FOUND"));

        TransactionRequest request = new TransactionRequest();
        request.setCardNum("9999999999999999");
        request.setAmt(new BigDecimal("10.00"));

        TransactionService.TransactionRejectedException ex = assertThrows(
                TransactionService.TransactionRejectedException.class,
                () -> transactionService.addTransaction(request));

        assertEquals(100, ex.getReasonCode());
    }

    @Test
    void addTransaction_accountNotFound_throws101() {
        when(validationService.validate(eq("4111111111111111"), any(BigDecimal.class), anyString()))
                .thenReturn(ValidationResult.rejected(101, "ACCOUNT RECORD NOT FOUND"));

        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("10.00"));

        TransactionService.TransactionRejectedException ex = assertThrows(
                TransactionService.TransactionRejectedException.class,
                () -> transactionService.addTransaction(request));

        assertEquals(101, ex.getReasonCode());
    }

    @Test
    void listTransactions_withDateRange_passesFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> page = new PageImpl<>(List.of(), pageable, 0);
        when(transactionRepository.findFiltered(null, "2026-01-01", "2026-12-31", pageable))
                .thenReturn(page);

        Page<TransactionResponse> result = transactionService.listTransactions(
                null, null, "2026-01-01", "2026-12-31", pageable);

        assertEquals(0, result.getTotalElements());
    }
}
