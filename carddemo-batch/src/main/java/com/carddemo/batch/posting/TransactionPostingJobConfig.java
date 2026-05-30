package com.carddemo.batch.posting;

import com.carddemo.common.entity.DailyTransaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TranCatBalanceRepository;
import com.carddemo.common.repository.TransactionRepository;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;

/**
 * Spring Batch configuration for the Transaction Posting job.
 * Mirrors COBOL program CBTRN02C (POSTTRAN): reads daily transactions,
 * validates each against business rules (XREF lookup, account lookup,
 * credit-limit, expiration), posts valid ones, rejects invalid ones.
 */
@Configuration
public class TransactionPostingJobConfig {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingJobConfig.class);
    private static final int CHUNK_SIZE = 100;

    @Value("${batch.posting.reject-path:rejected_transactions.dat}")
    private String rejectPath;

    @Bean
    public JpaPagingItemReader<DailyTransaction> dailyTransactionReader(
            EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<DailyTransaction>()
                .name("dailyTransactionReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DailyTransaction d ORDER BY d.tranId")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public TransactionPostingProcessor transactionPostingProcessor(
            CardXrefRepository cardXrefRepository,
            AccountRepository accountRepository) {
        return new TransactionPostingProcessor(cardXrefRepository, accountRepository);
    }

    @Bean
    public TransactionPostingWriter transactionPostingWriter(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CardXrefRepository cardXrefRepository,
            TranCatBalanceRepository tranCatBalanceRepository) {
        return new TransactionPostingWriter(
                transactionRepository,
                accountRepository,
                cardXrefRepository,
                tranCatBalanceRepository,
                Path.of(rejectPath));
    }

    @Bean
    public Step transactionPostingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaPagingItemReader<DailyTransaction> dailyTransactionReader,
            TransactionPostingProcessor transactionPostingProcessor,
            TransactionPostingWriter transactionPostingWriter) {
        return new StepBuilder("transactionPostingStep", jobRepository)
                .<DailyTransaction, PostingResult>chunk(CHUNK_SIZE, transactionManager)
                .reader(dailyTransactionReader)
                .processor(transactionPostingProcessor)
                .writer(transactionPostingWriter)
                .listener(postingStepListener(transactionPostingWriter))
                .build();
    }

    @Bean
    public Job transactionPostingJob(JobRepository jobRepository, Step transactionPostingStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(transactionPostingStep)
                .build();
    }

    private StepExecutionListener postingStepListener(TransactionPostingWriter writer) {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                log.info("START OF EXECUTION OF PROGRAM CBTRN02C (TransactionPostingJob)");
            }

            @Override
            public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
                log.info("TRANSACTIONS PROCESSED: {}", writer.getProcessedCount());
                log.info("TRANSACTIONS REJECTED : {}", writer.getRejectedCount());
                log.info("END OF EXECUTION OF PROGRAM CBTRN02C (TransactionPostingJob)");
                return null;
            }
        };
    }
}
