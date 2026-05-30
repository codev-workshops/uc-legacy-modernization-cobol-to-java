package com.carddemo.online.service;

import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TransactionRepository;
import com.carddemo.common.service.TransactionValidationService;
import com.carddemo.common.service.ValidationResult;
import com.carddemo.online.dto.TransactionRequest;
import com.carddemo.online.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionValidationService validationService;

    public TransactionService(TransactionRepository transactionRepository,
                              CardXrefRepository cardXrefRepository,
                              TransactionValidationService validationService) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.validationService = validationService;
    }

    public Page<TransactionResponse> listTransactions(Long accountId, String cardNum,
                                                      String startDate, String endDate,
                                                      Pageable pageable) {
        if (accountId != null) {
            List<String> cardNums = cardXrefRepository.findByAcctId(accountId).stream()
                    .map(CardXref::getXrefCardNum)
                    .toList();
            if (cardNums.isEmpty()) {
                return Page.empty(pageable);
            }
            return transactionRepository.findByCardNumsFiltered(cardNums, startDate, endDate, pageable)
                    .map(this::toResponse);
        }

        if (cardNum != null) {
            return transactionRepository.findFiltered(cardNum, startDate, endDate, pageable)
                    .map(this::toResponse);
        }

        return transactionRepository.findFiltered(null, startDate, endDate, pageable)
                .map(this::toResponse);
    }

    public TransactionResponse getTransaction(String tranId) {
        Transaction txn = transactionRepository.findById(tranId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found: " + tranId));
        return toResponse(txn);
    }

    @Transactional
    public TransactionResponse addTransaction(TransactionRequest request) {
        String now = LocalDateTime.now().format(TS_FORMAT);
        String tranDate = now.substring(0, 10);

        ValidationResult result = validationService.validate(
                request.getCardNum(), request.getAmt(), tranDate);

        if (!result.isAccepted()) {
            throw new TransactionRejectedException(
                    result.getReasonCode(), result.getReasonDescription());
        }

        Transaction txn = new Transaction();
        txn.setTranId(generateTranId());
        txn.setCardNum(request.getCardNum());
        txn.setTypeCd(request.getTypeCd());
        txn.setCatCd(request.getCatCd());
        txn.setSource(request.getSource());
        txn.setDesc(request.getDescription());
        txn.setAmt(request.getAmt());
        txn.setMerchantId(request.getMerchantId());
        txn.setMerchantName(request.getMerchantName());
        txn.setMerchantCity(request.getMerchantCity());
        txn.setMerchantZip(request.getMerchantZip());
        txn.setOrigTs(now);
        txn.setProcTs(now);

        transactionRepository.save(txn);
        return toResponse(txn);
    }

    private String generateTranId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private TransactionResponse toResponse(Transaction txn) {
        return new TransactionResponse(
                txn.getTranId(), txn.getTypeCd(), txn.getCatCd(), txn.getSource(),
                txn.getDesc(), txn.getAmt(), txn.getMerchantId(),
                txn.getMerchantName(), txn.getMerchantCity(), txn.getMerchantZip(),
                txn.getCardNum(), txn.getOrigTs(), txn.getProcTs());
    }

    public static class TransactionNotFoundException extends RuntimeException {
        public TransactionNotFoundException(String message) {
            super(message);
        }
    }

    public static class TransactionRejectedException extends RuntimeException {
        private final int reasonCode;

        public TransactionRejectedException(int reasonCode, String message) {
            super(message);
            this.reasonCode = reasonCode;
        }

        public int getReasonCode() { return reasonCode; }
    }
}
