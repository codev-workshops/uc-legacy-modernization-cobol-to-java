package com.carddemo.transaction.batch;

import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.transaction.entity.DisclosureGroup;
import com.carddemo.transaction.entity.TranCatBalance;
import com.carddemo.transaction.repository.DisclosureGroupRepository;
import com.carddemo.transaction.repository.TranCatBalanceRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InterestCalculationJob {

    private final TranCatBalanceRepository tranCatBalanceRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final AccountServiceClient accountServiceClient;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${carddemo.batch.chunk-size:100}")
    private int chunkSize;

    @Bean
    public Job interestJob(JobRepository jobRepository, Step interestStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestStep)
                .build();
    }

    @Bean
    public Step interestStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager) {
        return new StepBuilder("interestStep", jobRepository)
                .<TranCatBalance, TranCatBalance>chunk(chunkSize, transactionManager)
                .reader(interestReader())
                .processor(interestProcessor())
                .writer(interestWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<TranCatBalance> interestReader() {
        return new JpaPagingItemReaderBuilder<TranCatBalance>()
                .name("tranCatBalanceReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT t FROM TranCatBalance t ORDER BY t.trancatAcctId")
                .pageSize(chunkSize)
                .build();
    }

    @Bean
    public ItemProcessor<TranCatBalance, TranCatBalance> interestProcessor() {
        return new InterestProcessor();
    }

    @Bean
    public ItemWriter<TranCatBalance> interestWriter() {
        return items -> {
            for (TranCatBalance tcb : items) {
                tcb.setUpdatedAt(LocalDateTime.now());
                tranCatBalanceRepository.save(tcb);
                log.info("Updated balance for account {} type {} cat {}: {}",
                        tcb.getTrancatAcctId(), tcb.getTrancatTypeCd(),
                        tcb.getTrancatCd(), tcb.getTranCatBal());
            }
        };
    }

    public class InterestProcessor implements ItemProcessor<TranCatBalance, TranCatBalance> {

        @Override
        @CircuitBreaker(name = "accountService")
        public TranCatBalance process(TranCatBalance item) {
            log.debug("Calculating interest for account {} type {} cat {}",
                    item.getTrancatAcctId(), item.getTrancatTypeCd(), item.getTrancatCd());

            try {
                AccountDto account = accountServiceClient.getAccount(item.getTrancatAcctId());
                String acctGroupId = account != null ? account.getAcctGroupId() : null;

                if (acctGroupId == null) {
                    log.warn("No account group for account {}, skipping interest",
                            item.getTrancatAcctId());
                    return null;
                }

                Optional<DisclosureGroup> disclosure = disclosureGroupRepository
                        .findByDisAcctGroupIdAndDisTranTypeCdAndDisTranCatCd(
                                acctGroupId, item.getTrancatTypeCd(), item.getTrancatCd());

                if (disclosure.isEmpty()) {
                    log.warn("No disclosure group found for group {} type {} cat {}, skipping",
                            acctGroupId, item.getTrancatTypeCd(), item.getTrancatCd());
                    return null;
                }

                BigDecimal interestRate = disclosure.get().getDisIntRate();
                BigDecimal balance = item.getTranCatBal() != null
                        ? item.getTranCatBal() : BigDecimal.ZERO;

                BigDecimal interest = computeMonthlyInterest(balance, interestRate);
                item.setTranCatBal(balance.add(interest));

                updateAccountTotals(account, interest);

                return item;

            } catch (Exception e) {
                log.error("Error calculating interest for account {}: {}",
                        item.getTrancatAcctId(), e.getMessage());
                return null;
            }
        }
    }

    static BigDecimal computeMonthlyInterest(BigDecimal balance, BigDecimal annualRate) {
        if (balance.compareTo(BigDecimal.ZERO) <= 0 || annualRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return balance.multiply(annualRate)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    private void updateAccountTotals(AccountDto account, BigDecimal interest) {
        BigDecimal currBal = account.getAcctCurrBal() != null
                ? account.getAcctCurrBal() : BigDecimal.ZERO;
        account.setAcctCurrBal(currBal.add(interest));
        accountServiceClient.updateAccount(account.getAcctId(), account);
    }
}
