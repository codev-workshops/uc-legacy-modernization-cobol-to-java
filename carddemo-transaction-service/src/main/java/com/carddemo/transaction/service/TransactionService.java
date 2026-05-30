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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountServiceClient accountServiceClient;

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    public PagedResponse<TransactionDto> listTransactions(int page, int size) {
        Page<Transaction> txPage = transactionRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.<TransactionDto>builder()
                .content(txPage.getContent().stream()
                        .map(transactionMapper::toDto)
                        .toList())
                .page(txPage.getNumber())
                .size(txPage.getSize())
                .totalElements(txPage.getTotalElements())
                .totalPages(txPage.getTotalPages())
                .build();
    }

    public TransactionDto getTransaction(String tranId) {
        Transaction tx = transactionRepository.findById(tranId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found: " + tranId));
        return transactionMapper.toDto(tx);
    }

    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "createTransactionFallback")
    public TransactionDto createTransaction(TransactionDto dto) {
        log.info("Creating transaction for card {}", dto.getTranCardNum());

        CardXrefDto xref = accountServiceClient.getCardXref(dto.getTranCardNum());
        AccountDto account = accountServiceClient.getAccount(xref.getXrefAcctId());

        if (dto.getTranAmt() != null && account.getAcctCreditLimit() != null) {
            BigDecimal newBalance = (account.getAcctCurrBal() != null ? account.getAcctCurrBal() : BigDecimal.ZERO)
                    .add(dto.getTranAmt());
            if (newBalance.compareTo(account.getAcctCreditLimit()) > 0) {
                throw new BusinessValidationException(
                        "Transaction exceeds credit limit. Current balance: " +
                                account.getAcctCurrBal() + ", Credit limit: " +
                                account.getAcctCreditLimit());
            }
        }

        Transaction entity = transactionMapper.toEntity(dto);
        if (entity.getTranId() == null || entity.getTranId().isBlank()) {
            entity.setTranId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        }
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getTranOrigTs() == null) {
            entity.setTranOrigTs(LocalDateTime.now().format(TS_FORMAT));
        }
        if (entity.getTranProcTs() == null) {
            entity.setTranProcTs(LocalDateTime.now().format(TS_FORMAT));
        }

        Transaction saved = transactionRepository.save(entity);

        AccountDto updatedAccount = AccountDto.builder()
                .acctId(account.getAcctId())
                .acctActiveStatus(account.getAcctActiveStatus())
                .acctCurrBal((account.getAcctCurrBal() != null ? account.getAcctCurrBal() : BigDecimal.ZERO)
                        .add(dto.getTranAmt()))
                .acctCreditLimit(account.getAcctCreditLimit())
                .acctCashCreditLimit(account.getAcctCashCreditLimit())
                .acctOpenDate(account.getAcctOpenDate())
                .acctExpirationDate(account.getAcctExpirationDate())
                .acctReissueDate(account.getAcctReissueDate())
                .acctCurrCycCredit(account.getAcctCurrCycCredit())
                .acctCurrCycDebit(account.getAcctCurrCycDebit())
                .acctAddrZip(account.getAcctAddrZip())
                .acctGroupId(account.getAcctGroupId())
                .build();
        accountServiceClient.updateAccount(account.getAcctId(), updatedAccount);

        log.info("Transaction {} created successfully", saved.getTranId());
        return transactionMapper.toDto(saved);
    }

    public TransactionDto createTransactionFallback(TransactionDto dto, Throwable t) {
        log.error("Account service unavailable, cannot create transaction: {}", t.getMessage());
        throw new BusinessValidationException(
                "Account service is currently unavailable. Please try again later.");
    }
}
