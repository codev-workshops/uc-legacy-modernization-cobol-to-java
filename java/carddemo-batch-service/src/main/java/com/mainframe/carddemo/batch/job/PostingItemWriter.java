package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.BatchTransaction;
import com.mainframe.carddemo.batch.entity.DailyReject;
import com.mainframe.carddemo.batch.entity.TranCatBalance;
import com.mainframe.carddemo.batch.entity.TranCatBalanceId;
import com.mainframe.carddemo.batch.repository.BatchTransactionRepository;
import com.mainframe.carddemo.batch.repository.DailyRejectRepository;
import com.mainframe.carddemo.batch.repository.TranCatBalanceRepository;
import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.BalanceUpdateDto;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class PostingItemWriter implements ItemWriter<PostingResult> {

    private final BatchTransactionRepository transactionRepository;
    private final DailyRejectRepository rejectRepository;
    private final TranCatBalanceRepository tranCatBalanceRepository;
    private final AccountServiceClient accountServiceClient;

    public PostingItemWriter(BatchTransactionRepository transactionRepository,
                             DailyRejectRepository rejectRepository,
                             TranCatBalanceRepository tranCatBalanceRepository,
                             AccountServiceClient accountServiceClient) {
        this.transactionRepository = transactionRepository;
        this.rejectRepository = rejectRepository;
        this.tranCatBalanceRepository = tranCatBalanceRepository;
        this.accountServiceClient = accountServiceClient;
    }

    @Override
    public void write(Chunk<? extends PostingResult> chunk) {
        for (PostingResult result : chunk) {
            if (result.isRejected()) {
                writeReject(result);
            } else {
                writeValid(result);
            }
        }
    }

    private void writeValid(PostingResult result) {
        BatchTransaction txn = new BatchTransaction();
        txn.setTranId(result.getTransaction().getTranId());
        txn.setTranTypeCd(result.getTransaction().getTranTypeCd());
        txn.setTranCatCd(result.getTransaction().getTranCatCd());
        txn.setTranSource(result.getTransaction().getTranSource());
        txn.setTranDesc(result.getTransaction().getTranDesc());
        txn.setTranAmt(result.getTransaction().getTranAmt());
        txn.setTranMerchantId(result.getTransaction().getTranMerchantId());
        txn.setTranMerchantName(result.getTransaction().getTranMerchantName());
        txn.setTranMerchantCity(result.getTransaction().getTranMerchantCity());
        txn.setTranMerchantZip(result.getTransaction().getTranMerchantZip());
        txn.setTranCardNum(result.getTransaction().getTranCardNum());
        txn.setTranOrigTs(result.getTransaction().getTranOrigTs());
        txn.setTranProcTs(LocalDateTime.now());
        transactionRepository.save(txn);

        BigDecimal tranAmt = result.getTransaction().getTranAmt();
        BalanceUpdateDto balUpdate = new BalanceUpdateDto();
        BigDecimal currentCycleCredit = result.getCurrentCycleCredit() != null ? result.getCurrentCycleCredit() : BigDecimal.ZERO;
        BigDecimal currentCycleDebit = result.getCurrentCycleDebit() != null ? result.getCurrentCycleDebit() : BigDecimal.ZERO;
        BigDecimal currentBalance = currentCycleCredit.subtract(currentCycleDebit).add(tranAmt);

        if (tranAmt.compareTo(BigDecimal.ZERO) >= 0) {
            balUpdate.setCurrentCycleCredit(currentCycleCredit.add(tranAmt));
            balUpdate.setCurrentCycleDebit(currentCycleDebit);
        } else {
            balUpdate.setCurrentCycleCredit(currentCycleCredit);
            balUpdate.setCurrentCycleDebit(currentCycleDebit.add(tranAmt.abs()));
        }
        balUpdate.setCurrentBalance(currentBalance);

        accountServiceClient.updateAccountBalance(result.getAccountId(), balUpdate);

        updateTranCatBalance(result);
    }

    private void updateTranCatBalance(PostingResult result) {
        Long acctId = result.getAccountId();
        String typeCd = result.getTransaction().getTranTypeCd();
        Integer catCd = result.getTransaction().getTranCatCd();
        BigDecimal tranAmt = result.getTransaction().getTranAmt();

        TranCatBalanceId id = new TranCatBalanceId(acctId, typeCd, catCd);
        Optional<TranCatBalance> existingOpt = tranCatBalanceRepository.findById(id);

        if (existingOpt.isPresent()) {
            TranCatBalance existing = existingOpt.get();
            BigDecimal currentBal = existing.getTranCatBal() != null ? existing.getTranCatBal() : BigDecimal.ZERO;
            existing.setTranCatBal(currentBal.add(tranAmt));
            tranCatBalanceRepository.save(existing);
        } else {
            TranCatBalance newBal = new TranCatBalance();
            newBal.setTrancatAcctId(acctId);
            newBal.setTrancatTypeCd(typeCd);
            newBal.setTrancatCd(catCd);
            newBal.setTranCatBal(tranAmt);
            tranCatBalanceRepository.save(newBal);
        }
    }

    private void writeReject(PostingResult result) {
        DailyReject reject = new DailyReject();
        reject.setTranId(result.getTransaction().getTranId());
        reject.setRejectReason(result.getRejectReason());
        reject.setRejectDesc(result.getRejectDesc());
        reject.setRejectTs(LocalDateTime.now());
        rejectRepository.save(reject);
    }
}
