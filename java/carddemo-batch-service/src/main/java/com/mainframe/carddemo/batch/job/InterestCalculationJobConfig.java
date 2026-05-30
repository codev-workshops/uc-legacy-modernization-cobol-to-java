package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.TranCatBalance;
import com.mainframe.carddemo.batch.repository.BatchTransactionRepository;
import com.mainframe.carddemo.batch.repository.DisclosureGroupRepository;
import com.mainframe.carddemo.common.client.AccountServiceClient;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class InterestCalculationJobConfig {

    @Bean
    public JpaPagingItemReader<TranCatBalance> tranCatBalanceReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<TranCatBalance>()
                .name("tranCatBalanceReader")
                .entityManagerFactory(emf)
                .queryString("SELECT t FROM TranCatBalance t ORDER BY t.trancatAcctId")
                .pageSize(100)
                .build();
    }

    @Bean
    public InterestCalculationProcessor interestCalculationProcessor(
            AccountServiceClient accountServiceClient,
            DisclosureGroupRepository disclosureGroupRepository) {
        return new InterestCalculationProcessor(accountServiceClient, disclosureGroupRepository);
    }

    @Bean
    public InterestCalculationWriter interestCalculationWriter(
            BatchTransactionRepository transactionRepository,
            AccountServiceClient accountServiceClient) {
        return new InterestCalculationWriter(transactionRepository, accountServiceClient);
    }

    @Bean
    public Step interestCalculationStep(JobRepository jobRepository,
                                        PlatformTransactionManager txManager,
                                        JpaPagingItemReader<TranCatBalance> tranCatBalanceReader,
                                        InterestCalculationProcessor interestCalculationProcessor,
                                        InterestCalculationWriter interestCalculationWriter) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .<TranCatBalance, InterestResult>chunk(100, txManager)
                .reader(tranCatBalanceReader)
                .processor(interestCalculationProcessor)
                .writer(interestCalculationWriter)
                .listener(interestCalculationWriter)
                .build();
    }

    @Bean
    public Job interestCalculationJob(JobRepository jobRepository, Step interestCalculationStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep)
                .build();
    }
}
