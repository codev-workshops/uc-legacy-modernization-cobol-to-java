package com.carddemo.transaction.service;

import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.dto.CardXrefDto;
import com.carddemo.common.dto.TransactionDto;
import com.carddemo.common.exception.BusinessValidationException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.PagedResponse;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.mapper.TransactionMapper;
import com.carddemo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void listTransactions_returnsPagedResponse() {
        Transaction tx = Transaction.builder()
                .tranId("TX001")
                .tranTypeCd("01")
                .tranAmt(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();
        TransactionDto dto = TransactionDto.builder()
                .tranId("TX001")
                .tranTypeCd("01")
                .tranAmt(new BigDecimal("100.00"))
                .build();

        Page<Transaction> page = new PageImpl<>(List.of(tx), PageRequest.of(0, 20), 1);
        when(transactionRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(transactionMapper.toDto(tx)).thenReturn(dto);

        PagedResponse<TransactionDto> result = transactionService.listTransactions(0, 20);

        assertEquals(1, result.getContent().size());
        assertEquals("TX001", result.getContent().get(0).getTranId());
        assertEquals(0, result.getPage());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void getTransaction_found() {
        Transaction tx = Transaction.builder().tranId("TX001").build();
        TransactionDto dto = TransactionDto.builder().tranId("TX001").build();

        when(transactionRepository.findById("TX001")).thenReturn(Optional.of(tx));
        when(transactionMapper.toDto(tx)).thenReturn(dto);

        TransactionDto result = transactionService.getTransaction("TX001");
        assertEquals("TX001", result.getTranId());
    }

    @Test
    void getTransaction_notFound() {
        when(transactionRepository.findById("NOPE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransaction("NOPE"));
    }

    @Test
    void createTransaction_success() {
        TransactionDto dto = TransactionDto.builder()
                .tranCardNum("4111111111111111")
                .tranTypeCd("01")
                .tranAmt(new BigDecimal("50.00"))
                .build();

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1001L)
                .xrefCustId(2001L)
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(1001L)
                .acctCurrBal(new BigDecimal("100.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctActiveStatus("Y")
                .build();

        Transaction entity = Transaction.builder()
                .tranCardNum("4111111111111111")
                .tranTypeCd("01")
                .tranAmt(new BigDecimal("50.00"))
                .build();

        Transaction saved = Transaction.builder()
                .tranId("TX001")
                .tranCardNum("4111111111111111")
                .tranTypeCd("01")
                .tranAmt(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build();

        TransactionDto savedDto = TransactionDto.builder()
                .tranId("TX001")
                .tranCardNum("4111111111111111")
                .tranTypeCd("01")
                .tranAmt(new BigDecimal("50.00"))
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1001L)).thenReturn(account);
        when(transactionMapper.toEntity(dto)).thenReturn(entity);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
        when(transactionMapper.toDto(saved)).thenReturn(savedDto);
        when(accountServiceClient.updateAccount(eq(1001L), any(AccountDto.class))).thenReturn(account);

        TransactionDto result = transactionService.createTransaction(dto);

        assertEquals("TX001", result.getTranId());
        verify(accountServiceClient).getCardXref("4111111111111111");
        verify(accountServiceClient).getAccount(1001L);
        verify(accountServiceClient).updateAccount(eq(1001L), any(AccountDto.class));
    }

    @Test
    void createTransaction_exceedsCreditLimit() {
        TransactionDto dto = TransactionDto.builder()
                .tranCardNum("4111111111111111")
                .tranAmt(new BigDecimal("5000.00"))
                .build();

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1001L)
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(1001L)
                .acctCurrBal(new BigDecimal("4500.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1001L)).thenReturn(account);

        assertThrows(BusinessValidationException.class,
                () -> transactionService.createTransaction(dto));
    }

    @Test
    void createTransactionFallback_throwsBusinessValidation() {
        TransactionDto dto = TransactionDto.builder().build();
        assertThrows(BusinessValidationException.class,
                () -> transactionService.createTransactionFallback(dto, new RuntimeException("timeout")));
    }

    @Test
    void listTransactions_emptyPage() {
        Page<Transaction> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(transactionRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<TransactionDto> result = transactionService.listTransactions(0, 20);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}
