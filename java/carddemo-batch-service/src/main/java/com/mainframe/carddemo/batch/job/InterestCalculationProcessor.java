package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.BatchTransaction;
import com.mainframe.carddemo.batch.entity.DisclosureGroup;
import com.mainframe.carddemo.batch.entity.DisclosureGroupId;
import com.mainframe.carddemo.batch.entity.TranCatBalance;
import com.mainframe.carddemo.batch.repository.DisclosureGroupRepository;
import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InterestCalculationProcessor implements ItemProcessor<TranCatBalance, InterestResult> {

    private static final String DEFAULT_GROUP = "DEFAULT";
    private static final BigDecimal DIVISOR = new BigDecimal("1200");

    private final AccountServiceClient accountServiceClient;
    private final DisclosureGroupRepository disclosureGroupRepository;

    private Long lastAcctId;
    private AccountDto cachedAccount;
    private String cachedCardNum;
    private final AtomicLong tranIdSuffix = new AtomicLong(0);

    public InterestCalculationProcessor(AccountServiceClient accountServiceClient,
                                        DisclosureGroupRepository disclosureGroupRepository) {
        this.accountServiceClient = accountServiceClient;
        this.disclosureGroupRepository = disclosureGroupRepository;
    }

    @Override
    public InterestResult process(TranCatBalance item) {
        Long acctId = item.getTrancatAcctId();

        if (!acctId.equals(lastAcctId)) {
            cachedAccount = accountServiceClient.getInternalAccountById(acctId);
            List<CardXrefDto> xrefs = accountServiceClient.getXrefByAccountId(acctId);
            cachedCardNum = (xrefs != null && !xrefs.isEmpty()) ? xrefs.get(0).getCardNum() : "";
            lastAcctId = acctId;
        }

        if (cachedAccount == null) {
            return null;
        }

        String groupId = cachedAccount.getGroupId();
        if (groupId == null) {
            groupId = DEFAULT_GROUP;
        }

        BigDecimal rate = lookupRate(groupId, item.getTrancatTypeCd(), item.getTrancatCd());
        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal catBal = item.getTranCatBal();
        if (catBal == null) {
            return null;
        }

        BigDecimal monthlyInterest = catBal.multiply(rate)
                .divide(DIVISOR, 2, RoundingMode.HALF_UP);

        BatchTransaction tx = buildInterestTransaction(acctId, monthlyInterest);
        return new InterestResult(acctId, monthlyInterest, tx);
    }

    private BigDecimal lookupRate(String groupId, String tranTypeCd, Integer tranCatCd) {
        DisclosureGroupId id = new DisclosureGroupId(groupId, tranTypeCd, tranCatCd);
        Optional<DisclosureGroup> dg = disclosureGroupRepository.findById(id);

        if (dg.isEmpty() && !DEFAULT_GROUP.equals(groupId)) {
            DisclosureGroupId defaultId = new DisclosureGroupId(DEFAULT_GROUP, tranTypeCd, tranCatCd);
            dg = disclosureGroupRepository.findById(defaultId);
        }

        return dg.map(DisclosureGroup::getDisIntRate).orElse(null);
    }

    private BatchTransaction buildInterestTransaction(Long acctId, BigDecimal monthlyInterest) {
        long suffix = tranIdSuffix.incrementAndGet();
        LocalDateTime now = LocalDateTime.now();

        BatchTransaction tx = new BatchTransaction();
        tx.setTranId(String.format("INT%013d", suffix));
        tx.setTranTypeCd("01");
        tx.setTranCatCd(5);
        tx.setTranSource("System");
        tx.setTranDesc("Int. for a/c " + acctId);
        tx.setTranAmt(monthlyInterest);
        tx.setTranMerchantId(0L);
        tx.setTranCardNum(cachedCardNum);
        tx.setTranOrigTs(now);
        tx.setTranProcTs(now);
        return tx;
    }
}
