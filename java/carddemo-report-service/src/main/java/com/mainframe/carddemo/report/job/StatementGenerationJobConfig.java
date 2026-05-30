package com.mainframe.carddemo.report.job;

import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.client.TransactionServiceClient;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import com.mainframe.carddemo.report.repository.GeneratedReportRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class StatementGenerationJobConfig {

    @Bean
    public CardXrefItemReader cardXrefItemReader(AccountServiceClient accountServiceClient) {
        return new CardXrefItemReader(accountServiceClient);
    }

    @Bean
    public StatementGenerationProcessor statementGenerationProcessor(
            AccountServiceClient accountServiceClient,
            TransactionServiceClient transactionServiceClient) {
        return new StatementGenerationProcessor(accountServiceClient, transactionServiceClient);
    }

    @Bean
    public StatementGenerationWriter statementGenerationWriter(GeneratedReportRepository reportRepository) {
        return new StatementGenerationWriter(reportRepository);
    }

    @Bean
    public Step statementGenerationStep(JobRepository jobRepository,
                                        PlatformTransactionManager txManager,
                                        CardXrefItemReader cardXrefItemReader,
                                        StatementGenerationProcessor statementGenerationProcessor,
                                        StatementGenerationWriter statementGenerationWriter) {
        return new StepBuilder("statementGenerationStep", jobRepository)
                .<CardXrefDto, StatementData>chunk(10, txManager)
                .reader(cardXrefItemReader)
                .processor(statementGenerationProcessor)
                .writer(statementGenerationWriter)
                .build();
    }

    @Bean
    public Job statementGenerationJob(JobRepository jobRepository, Step statementGenerationStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .start(statementGenerationStep)
                .build();
    }
}
