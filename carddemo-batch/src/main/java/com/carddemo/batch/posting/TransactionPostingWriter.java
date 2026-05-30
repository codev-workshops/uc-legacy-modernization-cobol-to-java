package com.carddemo.batch.posting;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.DailyTransaction;
import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.entity.TranCatBalanceId;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TranCatBalanceRepository;
import com.carddemo.common.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Writes accepted transactions to the DB (transaction record, account update,
 * tran-category-balance update) and rejected transactions to a reject file.
 * Mirrors CBTRN02C paragraphs 2000, 2700, 2800, 2900, 2500.
 */
public class TransactionPostingWriter implements ItemWriter<PostingResult> {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingWriter.class);
    private static final DateTimeFormatter PROC_TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS0000");

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TranCatBalanceRepository tranCatBalanceRepository;
    private final Path rejectFilePath;

    private final AtomicLong processedCount = new AtomicLong();
    private final AtomicLong rejectedCount = new AtomicLong();

    public TransactionPostingWriter(TransactionRepository transactionRepository,
                                    AccountRepository accountRepository,
                                    CardXrefRepository cardXrefRepository,
                                    TranCatBalanceRepository tranCatBalanceRepository,
                                    Path rejectFilePath) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.tranCatBalanceRepository = tranCatBalanceRepository;
        this.rejectFilePath = rejectFilePath;
    }

    @Override
    public void write(Chunk<? extends PostingResult> chunk) throws Exception {
        for (PostingResult result : chunk) {
            if (result.isAccepted()) {
                postTransaction(result.getSource());
                processedCount.incrementAndGet();
            } else {
                writeReject(result.getRejected());
                rejectedCount.incrementAndGet();
            }
        }
    }

    /**
     * 2000-POST-TRANSACTION: map daily tran → transaction record, update
     * TCATBAL, update account, write transaction.
     */
    private void postTransaction(DailyTransaction dt) {
        // Build posted Transaction (2000-POST-TRANSACTION, lines 424-443)
        Transaction txn = new Transaction();
        txn.setTranId(dt.getTranId());
        txn.setTypeCd(dt.getTypeCd());
        txn.setCatCd(dt.getCatCd());
        txn.setSource(dt.getSource());
        txn.setDesc(dt.getDesc());
        txn.setAmt(dt.getAmt());
        txn.setMerchantId(dt.getMerchantId());
        txn.setMerchantName(dt.getMerchantName());
        txn.setMerchantCity(dt.getMerchantCity());
        txn.setMerchantZip(dt.getMerchantZip());
        txn.setCardNum(dt.getCardNum());
        txn.setOrigTs(dt.getOrigTs());
        txn.setProcTs(LocalDateTime.now().format(PROC_TS_FMT));

        // Resolve account via XREF
        CardXref xref = cardXrefRepository.findById(dt.getCardNum()).orElseThrow();
        Long acctId = xref.getAcctId();

        // 2700-UPDATE-TCATBAL
        updateTranCatBalance(acctId, dt);

        // 2800-UPDATE-ACCOUNT-REC
        updateAccount(acctId, dt.getAmt());

        // 2900-WRITE-TRANSACTION-FILE
        transactionRepository.save(txn);
    }

    /**
     * 2700-UPDATE-TCATBAL: read or create TRAN-CAT-BAL-RECORD for
     * (acctId, typeCd, catCd), then add DALYTRAN-AMT to TRAN-CAT-BAL.
     */
    private void updateTranCatBalance(Long acctId, DailyTransaction dt) {
        TranCatBalanceId id = new TranCatBalanceId(acctId, dt.getTypeCd(), dt.getCatCd());
        Optional<TranCatBalance> existing = tranCatBalanceRepository.findById(id);

        TranCatBalance tcb;
        if (existing.isPresent()) {
            tcb = existing.get();
            BigDecimal current = tcb.getTranCatBal() != null ? tcb.getTranCatBal() : BigDecimal.ZERO;
            tcb.setTranCatBal(current.add(dt.getAmt()));
        } else {
            tcb = new TranCatBalance();
            tcb.setAcctId(acctId);
            tcb.setTypeCd(dt.getTypeCd());
            tcb.setCatCd(dt.getCatCd());
            tcb.setTranCatBal(dt.getAmt());
            log.info("Creating TCATBAL record for key {}/{}/{}", acctId, dt.getTypeCd(), dt.getCatCd());
        }
        tranCatBalanceRepository.save(tcb);
    }

    /**
     * 2800-UPDATE-ACCOUNT-REC: add DALYTRAN-AMT to ACCT-CURR-BAL, and to
     * either ACCT-CURR-CYC-CREDIT (>= 0) or ACCT-CURR-CYC-DEBIT (< 0).
     */
    private void updateAccount(Long acctId, BigDecimal amt) {
        Account acct = accountRepository.findById(acctId).orElseThrow();

        BigDecimal currBal = acct.getCurrBal() != null ? acct.getCurrBal() : BigDecimal.ZERO;
        acct.setCurrBal(currBal.add(amt));

        if (amt.compareTo(BigDecimal.ZERO) >= 0) {
            BigDecimal cycCredit = acct.getCurrCycCredit() != null ? acct.getCurrCycCredit() : BigDecimal.ZERO;
            acct.setCurrCycCredit(cycCredit.add(amt));
        } else {
            BigDecimal cycDebit = acct.getCurrCycDebit() != null ? acct.getCurrCycDebit() : BigDecimal.ZERO;
            acct.setCurrCycDebit(cycDebit.add(amt));
        }

        accountRepository.save(acct);
    }

    /**
     * 2500-WRITE-REJECT-REC: append the rejected transaction + trailer to the
     * rejects file.
     */
    private void writeReject(RejectedTransaction rj) throws IOException {
        Files.createDirectories(rejectFilePath.getParent());
        try (BufferedWriter bw = Files.newBufferedWriter(rejectFilePath,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            String line = String.format("%s|%04d|%s",
                    rj.getTransaction().getTranId(), rj.getReasonCode(), rj.getReasonDesc());
            bw.write(line);
            bw.newLine();
        }
    }

    public long getProcessedCount() { return processedCount.get(); }
    public long getRejectedCount() { return rejectedCount.get(); }
}
