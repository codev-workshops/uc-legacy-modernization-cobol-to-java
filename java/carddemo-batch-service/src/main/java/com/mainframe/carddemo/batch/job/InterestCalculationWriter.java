package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.repository.BatchTransactionRepository;
import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.BalanceUpdateDto;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class InterestCalculationWriter implements ItemWriter<InterestResult>, StepExecutionListener {

    private final BatchTransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final Map<Long, BigDecimal> accountInterestTotals = new LinkedHashMap<>();

    public InterestCalculationWriter(BatchTransactionRepository transactionRepository,
                                     AccountServiceClient accountServiceClient) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
    }

    @Override
    public void write(Chunk<? extends InterestResult> chunk) {
        for (InterestResult result : chunk) {
            transactionRepository.save(result.getInterestTransaction());
            accountInterestTotals.merge(
                    result.getAcctId(),
                    result.getMonthlyInterest(),
                    BigDecimal::add
            );
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        for (Map.Entry<Long, BigDecimal> entry : accountInterestTotals.entrySet()) {
            Long acctId = entry.getKey();
            BigDecimal totalInterest = entry.getValue();

            AccountDto account = accountServiceClient.getInternalAccountById(acctId);
            if (account != null) {
                BigDecimal currentBal = account.getCurrentBalance() != null
                        ? account.getCurrentBalance() : BigDecimal.ZERO;

                BalanceUpdateDto update = new BalanceUpdateDto();
                update.setCurrentBalance(currentBal.add(totalInterest));
                update.setCurrentCycleCredit(BigDecimal.ZERO);
                update.setCurrentCycleDebit(BigDecimal.ZERO);
                accountServiceClient.updateAccountBalance(acctId, update);
            }
        }
        accountInterestTotals.clear();
        return ExitStatus.COMPLETED;
    }
}
