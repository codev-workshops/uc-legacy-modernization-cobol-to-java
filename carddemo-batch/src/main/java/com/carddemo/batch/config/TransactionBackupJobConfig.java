package com.carddemo.batch.config;

import com.carddemo.common.entity.Transaction;
import com.carddemo.batch.writer.TransactionBackupFileWriter;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
 * Spring Batch configuration for the Transaction Backup job.
 * Mirrors COBOL program CBTRN01C: reads all transactions and writes
 * them to a sequential backup file.
 */
@Configuration
public class TransactionBackupJobConfig {

    private static final int CHUNK_SIZE = 100;

    @Value("${batch.backup.output-path:transaction_backup.dat}")
    private String outputPath;

    @Bean
    public JpaPagingItemReader<Transaction> transactionReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<Transaction>()
                .name("transactionReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT t FROM Transaction t ORDER BY t.tranId")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public TransactionBackupFileWriter transactionBackupWriter() {
        return new TransactionBackupFileWriter(Path.of(outputPath));
    }

    @Bean
    public Step transactionBackupStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      JpaPagingItemReader<Transaction> transactionReader,
                                      TransactionBackupFileWriter transactionBackupWriter) {
        return new StepBuilder("transactionBackupStep", jobRepository)
                .<Transaction, Transaction>chunk(CHUNK_SIZE, transactionManager)
                .reader(transactionReader)
                .writer(transactionBackupWriter)
                .build();
    }

    @Bean
    public Job transactionBackupJob(JobRepository jobRepository, Step transactionBackupStep) {
        return new JobBuilder("transactionBackupJob", jobRepository)
                .start(transactionBackupStep)
                .build();
    }
}
