package com.carddemo.batch.job.interest;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.DisclosureGroup;
import com.carddemo.common.entity.DisclosureGroupId;
import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.DisclosureGroupRepository;
import com.carddemo.common.repository.TranCatBalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tasklet that mirrors CBACT04C (INTCALC): for each account, iterates
 * through its tran_cat_balance entries, looks up the interest rate from
 * the disclosure_groups table, computes monthly interest, and updates
 * the account balance. After interest is applied, cycle fields
 * (currCycCredit, currCycDebit) are reset to zero (Business Rule 7).
 */
@Component
public class InterestCalculationTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationTasklet.class);
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("1200");
    private static final String DEFAULT_GROUP_ID = "DEFAULT";

    private final TranCatBalanceRepository tranCatBalanceRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final AccountRepository accountRepository;

    public InterestCalculationTasklet(TranCatBalanceRepository tranCatBalanceRepository,
                                      DisclosureGroupRepository disclosureGroupRepository,
                                      AccountRepository accountRepository) {
        this.tranCatBalanceRepository = tranCatBalanceRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("START OF EXECUTION OF INTEREST CALCULATION JOB");

        List<TranCatBalance> allBalances = new ArrayList<>(tranCatBalanceRepository.findAll());
        allBalances.sort((a, b) -> {
            int cmp = Long.compare(a.getAcctId(), b.getAcctId());
            if (cmp != 0) return cmp;
            cmp = a.getTypeCd().compareTo(b.getTypeCd());
            if (cmp != 0) return cmp;
            return Integer.compare(a.getCatCd(), b.getCatCd());
        });

        Long lastAcctId = null;
        Account currentAccount = null;
        BigDecimal totalInterest = BigDecimal.ZERO;
        int recordCount = 0;

        for (TranCatBalance tcb : allBalances) {
            recordCount++;

            if (!tcb.getAcctId().equals(lastAcctId)) {
                if (currentAccount != null) {
                    updateAccount(currentAccount, totalInterest);
                }
                totalInterest = BigDecimal.ZERO;
                lastAcctId = tcb.getAcctId();
                currentAccount = accountRepository.findById(tcb.getAcctId()).orElse(null);
                if (currentAccount == null) {
                    log.warn("Account not found: {}", tcb.getAcctId());
                    continue;
                }
            }

            if (currentAccount == null) {
                continue;
            }

            BigDecimal rate = getInterestRate(currentAccount.getGroupId(),
                    tcb.getTypeCd(), tcb.getCatCd());

            if (rate.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal monthlyInterest = computeMonthlyInterest(
                        tcb.getTranCatBal(), rate);
                totalInterest = totalInterest.add(monthlyInterest);
            }
        }

        if (currentAccount != null) {
            updateAccount(currentAccount, totalInterest);
        }

        log.info("END OF EXECUTION OF INTEREST CALCULATION JOB. Records processed: {}",
                recordCount);
        return RepeatStatus.FINISHED;
    }

    /**
     * Computes monthly interest: (balance * rate) / 1200.
     * The rate is an annual percentage (e.g. 18.00 means 18%).
     * Dividing by 1200 converts to a monthly factor.
     */
    BigDecimal computeMonthlyInterest(BigDecimal balance, BigDecimal annualRate) {
        if (balance == null || annualRate == null) {
            return BigDecimal.ZERO;
        }
        return balance.multiply(annualRate)
                .divide(MONTHS_IN_YEAR, 2, RoundingMode.HALF_UP);
    }

    /**
     * Looks up the interest rate from the disclosure_groups table.
     * Falls back to DEFAULT group if the account's group is not found
     * (mirrors CBACT04C lines 436-439).
     */
    BigDecimal getInterestRate(String groupId, String typeCd, Integer catCd) {
        if (groupId == null) {
            return BigDecimal.ZERO;
        }
        DisclosureGroupId id = new DisclosureGroupId(groupId, typeCd, catCd);
        Optional<DisclosureGroup> dg = disclosureGroupRepository.findById(id);

        if (dg.isEmpty()) {
            DisclosureGroupId defaultId = new DisclosureGroupId(DEFAULT_GROUP_ID, typeCd, catCd);
            dg = disclosureGroupRepository.findById(defaultId);
        }

        return dg.map(DisclosureGroup::getIntRate).orElse(BigDecimal.ZERO);
    }

    /**
     * Updates the account balance with accumulated interest and resets
     * cycle fields to zero (Business Rule 7: CBACT04C lines 350-354).
     */
    private void updateAccount(Account account, BigDecimal totalInterest) {
        BigDecimal currentBalance = account.getCurrBal() != null
                ? account.getCurrBal() : BigDecimal.ZERO;
        account.setCurrBal(currentBalance.add(totalInterest));
        account.setCurrCycCredit(BigDecimal.ZERO);
        account.setCurrCycDebit(BigDecimal.ZERO);
        accountRepository.save(account);
        log.debug("Updated account {}: interest={}, newBalance={}",
                account.getAcctId(), totalInterest, account.getCurrBal());
    }
}
