package com.mainframe.carddemo.transaction.service;

import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import com.mainframe.carddemo.transaction.entity.Transaction;
import com.mainframe.carddemo.transaction.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountServiceClient accountServiceClient) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
    }

    public Page<Transaction> getTransactionsByAccount(Long acctId, int page, int size) {
        List<CardXrefDto> xrefs = accountServiceClient.getXrefByAccountId(acctId);
        if (xrefs.isEmpty()) {
            return Page.empty();
        }
        List<String> cardNums = xrefs.stream()
                .map(CardXrefDto::getCardNum)
                .collect(Collectors.toList());
        return transactionRepository.findByTranCardNumIn(
                cardNums,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "tranOrigTs"))
        );
    }

    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
    }

    public Transaction createTransaction(TransactionCreateRequest request) {
        CardXrefDto xref = accountServiceClient.getXrefByCardNum(request.getCardNum());
        if (xref == null) {
            throw new ResourceNotFoundException("Card not found in cross-reference: " + request.getCardNum());
        }

        AccountDto account = accountServiceClient.getInternalAccountById(xref.getAccountId());
        if (account == null) {
            throw new ResourceNotFoundException("Account not found: " + xref.getAccountId());
        }

        Transaction transaction = new Transaction();
        transaction.setTranId(generateTranId());
        transaction.setTranTypeCd(request.getTranTypeCd());
        transaction.setTranCatCd(request.getTranCatCd());
        transaction.setTranSource(request.getTranSource());
        transaction.setTranDesc(request.getTranDesc());
        transaction.setTranAmt(request.getTranAmt());
        transaction.setTranMerchantName(request.getTranMerchantName());
        transaction.setTranMerchantCity(request.getTranMerchantCity());
        transaction.setTranMerchantZip(request.getTranMerchantZip());
        transaction.setTranCardNum(request.getCardNum());
        transaction.setTranOrigTs(LocalDateTime.now());
        transaction.setTranProcTs(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    public Transaction processBillPayment(BillPaymentRequest request) {
        CardXrefDto xref = accountServiceClient.getXrefByCardNum(request.getCardNum());
        if (xref == null) {
            throw new ResourceNotFoundException("Card not found in cross-reference: " + request.getCardNum());
        }

        AccountDto account = accountServiceClient.getInternalAccountById(xref.getAccountId());
        if (account == null) {
            throw new ResourceNotFoundException("Account not found: " + xref.getAccountId());
        }

        Transaction transaction = new Transaction();
        transaction.setTranId(generateTranId());
        transaction.setTranTypeCd("02");
        transaction.setTranCatCd(2);
        transaction.setTranSource("ONLINE");
        transaction.setTranDesc("Bill Payment");
        transaction.setTranAmt(request.getAmount().negate());
        transaction.setTranCardNum(request.getCardNum());
        transaction.setTranOrigTs(LocalDateTime.now());
        transaction.setTranProcTs(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    private String generateTranId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.substring(0, 16);
    }
}
