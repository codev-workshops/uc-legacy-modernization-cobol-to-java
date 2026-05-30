package com.carddemo.online.service;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TransactionRepository;
import com.carddemo.online.dto.BillPaymentRequest;
import com.carddemo.online.dto.BillPaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Migrated from COBIL00C.cbl — processes online bill payments.
 * Validates account, reduces balance, and creates a transaction record.
 */
@Service
public class BillPaymentService {

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS0000");
    private static final String TRAN_TYPE_CD = "02";
    private static final int TRAN_CAT_CD = 2;
    private static final String TRAN_DESC = "BILL PAYMENT - ONLINE";
    private static final long TRAN_MERCHANT_ID = 999999999L;
    private static final String TRAN_MERCHANT_NAME = "BILL PAYMENT";
    private static final String TRAN_MERCHANT_CITY = "N/A";
    private static final String TRAN_MERCHANT_ZIP = "N/A";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;

    public BillPaymentService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              CardXrefRepository cardXrefRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    @Transactional
    public BillPaymentResponse processPayment(BillPaymentRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account ID not found: " + request.getAccountId()));

        if (!"Y".equals(account.getActiveStatus())) {
            throw new AccountNotActiveException(
                    "Account is not active: " + request.getAccountId());
        }

        BigDecimal paymentAmount = request.getPaymentAmount();
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Payment amount must be positive");
        }

        // Resolve card number via XREF (mirrors READ-CXACAIX-FILE)
        String cardNum = resolveCardNum(request.getAccountId());

        // Generate next transaction ID (mirrors STARTBR/READPREV TRANSACT logic)
        String tranId = generateNextTranId();

        // Build transaction record (mirrors COBIL00C lines 218-232)
        String now = LocalDateTime.now().format(TS_FMT);
        String source = request.getPaymentSource() != null
                ? request.getPaymentSource() : "POS TERM";

        Transaction txn = new Transaction();
        txn.setTranId(tranId);
        txn.setTypeCd(TRAN_TYPE_CD);
        txn.setCatCd(TRAN_CAT_CD);
        txn.setSource(source.length() > 10 ? source.substring(0, 10) : source);
        txn.setDesc(TRAN_DESC);
        txn.setAmt(paymentAmount);
        txn.setCardNum(cardNum);
        txn.setMerchantId(TRAN_MERCHANT_ID);
        txn.setMerchantName(TRAN_MERCHANT_NAME);
        txn.setMerchantCity(TRAN_MERCHANT_CITY);
        txn.setMerchantZip(TRAN_MERCHANT_ZIP);
        txn.setOrigTs(now);
        txn.setProcTs(now);

        transactionRepository.save(txn);

        // Update account balance (mirrors COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT
        // and 2800-UPDATE-ACCOUNT-REC from TransactionPostingWriter)
        BigDecimal currBal = account.getCurrBal() != null ? account.getCurrBal() : BigDecimal.ZERO;
        BigDecimal newBalance = currBal.subtract(paymentAmount);
        account.setCurrBal(newBalance);

        // Payment is a credit to the account (reduces balance)
        BigDecimal cycCredit = account.getCurrCycCredit() != null
                ? account.getCurrCycCredit() : BigDecimal.ZERO;
        account.setCurrCycCredit(cycCredit.add(paymentAmount));

        accountRepository.save(account);

        return new BillPaymentResponse(
                tranId,
                account.getAcctId(),
                paymentAmount,
                newBalance,
                "Bill payment processed successfully"
        );
    }

    private String resolveCardNum(Long accountId) {
        List<CardXref> xrefs = cardXrefRepository.findAll().stream()
                .filter(x -> accountId.equals(x.getAcctId()))
                .toList();
        if (xrefs.isEmpty()) {
            return "0000000000000000";
        }
        return xrefs.get(0).getXrefCardNum();
    }

    private String generateNextTranId() {
        List<Transaction> all = transactionRepository.findAll();
        long maxId = 0;
        for (Transaction t : all) {
            try {
                long id = Long.parseLong(t.getTranId().trim());
                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException ignored) {
                // skip non-numeric IDs
            }
        }
        return String.format("%016d", maxId + 1);
    }

    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(String message) { super(message); }
    }

    public static class AccountNotActiveException extends RuntimeException {
        public AccountNotActiveException(String message) { super(message); }
    }

    public static class InvalidPaymentException extends RuntimeException {
        public InvalidPaymentException(String message) { super(message); }
    }
}
