package com.carddemo.batch.statement;

import com.carddemo.common.entity.CardXref;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.common.repository.TransactionRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job configuration for statement generation.
 *
 * <p>Migrates COBOL programs CBSTM03A (main driver) and CBSTM03B (file I/O
 * subroutine). Reads the {@code card_xref} table, enriches each entry with
 * customer/account/transaction data, and writes formatted plain-text
 * statements to a file.
 *
 * <h3>Job Parameters</h3>
 * <ul>
 *   <li>{@code startDate} — statement period start (yyyy-MM-dd)</li>
 *   <li>{@code endDate} — statement period end (yyyy-MM-dd)</li>
 *   <li>{@code outputPath} — path for the statement text file</li>
 * </ul>
 */
@Configuration
public class StatementGenerationJobConfig {

    @Bean
    public Job statementGenerationJob(JobRepository jobRepository, Step statementGenerationStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .start(statementGenerationStep)
                .build();
    }

    @Bean
    public Step statementGenerationStep(JobRepository jobRepository,
                                        PlatformTransactionManager txManager,
                                        JpaCursorItemReader<CardXref> cardXrefReader,
                                        StatementProcessor statementProcessor,
                                        StatementWriter statementWriter) {
        return new StepBuilder("statementGenerationStep", jobRepository)
                .<CardXref, StatementData>chunk(10, txManager)
                .reader(cardXrefReader)
                .processor(statementProcessor)
                .writer(statementWriter)
                .listener(statementWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<CardXref> cardXrefReader(EntityManagerFactory emf) {
        JpaCursorItemReader<CardXref> reader = new JpaCursorItemReader<>();
        reader.setEntityManagerFactory(emf);
        reader.setQueryString("SELECT x FROM CardXref x ORDER BY x.xrefCardNum");
        return reader;
    }

    @Bean
    @StepScope
    public StatementProcessor statementProcessor(
            CustomerRepository customerRepo,
            AccountRepository accountRepo,
            TransactionRepository transactionRepo,
            @Value("#{jobParameters['startDate']}") String startDate,
            @Value("#{jobParameters['endDate']}") String endDate) {
        return new StatementProcessor(customerRepo, accountRepo, transactionRepo,
                startDate, endDate);
    }

    @Bean
    @StepScope
    public StatementWriter statementWriter(
            @Value("#{jobParameters['outputPath']}") String outputPath,
            StatementFormatterService formatterService) {
        return new StatementWriter(outputPath, formatterService);
    }
}
