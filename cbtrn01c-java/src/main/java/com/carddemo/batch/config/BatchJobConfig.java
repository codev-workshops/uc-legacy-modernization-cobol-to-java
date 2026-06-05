package com.carddemo.batch.config;

import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.processor.TransactionValidationProcessor;
import com.carddemo.batch.reader.DailyTransactionReader;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.XrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Spring Batch configuration for the CBTRN01C daily transaction validation job.
 * Defines a single-step job: read → validate → log.
 */
@Configuration
public class BatchJobConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchJobConfig.class);

    @Value("${app.files.dalytran}")
    private String dalytranFile;

    @Value("${app.files.xreffile}")
    private String xrefFile;

    @Value("${app.files.acctfile}")
    private String acctFile;

    @Value("${app.chunk-size:10}")
    private int chunkSize;

    @Bean
    public XrefRepository xrefRepository() throws IOException {
        return new XrefRepository(Path.of(xrefFile));
    }

    @Bean
    public AccountRepository accountRepository() throws IOException {
        return new AccountRepository(Path.of(acctFile));
    }

    @Bean
    public DailyTransactionReader dailyTransactionReader() {
        return new DailyTransactionReader(Path.of(dalytranFile));
    }

    @Bean
    public TransactionValidationProcessor transactionValidationProcessor(
            XrefRepository xrefRepository, AccountRepository accountRepository) {
        return new TransactionValidationProcessor(xrefRepository, accountRepository);
    }

    @Bean
    public ItemWriter<DailyTransaction> transactionWriter() {
        return items -> {
            for (DailyTransaction txn : items) {
                log.info("VALIDATED: Transaction ID={} Card={} Amount={}",
                        txn.id(), txn.cardNum(), txn.amt());
            }
        };
    }

    @Bean
    public Step validateStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             DailyTransactionReader reader,
                             TransactionValidationProcessor processor,
                             ItemWriter<DailyTransaction> writer) {
        return new StepBuilder("validateDailyTransactionsStep", jobRepository)
                .<DailyTransaction, DailyTransaction>chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job validateDailyTransactionsJob(JobRepository jobRepository, Step validateStep) {
        return new JobBuilder("validateDailyTransactionsJob", jobRepository)
                .start(validateStep)
                .build();
    }
}
