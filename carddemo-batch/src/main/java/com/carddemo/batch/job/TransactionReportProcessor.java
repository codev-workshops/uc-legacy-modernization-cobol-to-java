package com.carddemo.batch.job;

import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.TranCategory;
import com.carddemo.common.entity.TranCategoryId;
import com.carddemo.common.entity.TranType;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TranCategoryRepository;
import com.carddemo.common.repository.TranTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Optional;

/**
 * Enriches each Transaction with account ID (from CardXref), transaction type
 * description, and transaction category description via repository lookups.
 */
public class TransactionReportProcessor implements ItemProcessor<Transaction, TransactionReportItem> {

    private static final Logger log = LoggerFactory.getLogger(TransactionReportProcessor.class);

    private final CardXrefRepository cardXrefRepository;
    private final TranTypeRepository tranTypeRepository;
    private final TranCategoryRepository tranCategoryRepository;

    public TransactionReportProcessor(CardXrefRepository cardXrefRepository,
                                      TranTypeRepository tranTypeRepository,
                                      TranCategoryRepository tranCategoryRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.tranTypeRepository = tranTypeRepository;
        this.tranCategoryRepository = tranCategoryRepository;
    }

    @Override
    public TransactionReportItem process(Transaction tx) {
        TransactionReportItem item = new TransactionReportItem();
        item.setTranId(tx.getTranId());
        item.setCardNum(tx.getCardNum());
        item.setTypeCd(tx.getTypeCd());
        item.setCatCd(tx.getCatCd() != null ? tx.getCatCd() : 0);
        item.setSource(tx.getSource());
        item.setAmount(tx.getAmt());

        Optional<CardXref> xref = cardXrefRepository.findById(
                tx.getCardNum() != null ? tx.getCardNum() : "");
        if (xref.isPresent()) {
            item.setAccountId(String.format("%011d", xref.get().getAcctId()));
        } else {
            log.warn("CardXref not found for card {}", tx.getCardNum());
            item.setAccountId("N/A");
        }

        Optional<TranType> tranType = tranTypeRepository.findById(
                tx.getTypeCd() != null ? tx.getTypeCd() : "");
        item.setTypeDesc(tranType.map(TranType::getTranTypeDesc).orElse(""));

        TranCategoryId catId = new TranCategoryId(
                tx.getTypeCd() != null ? tx.getTypeCd() : "",
                tx.getCatCd() != null ? tx.getCatCd() : 0);
        Optional<TranCategory> tranCat = tranCategoryRepository.findById(catId);
        item.setCatDesc(tranCat.map(TranCategory::getTranCatTypeDesc).orElse(""));

        return item;
    }
}
