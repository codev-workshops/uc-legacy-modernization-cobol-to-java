package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountOutputBundle;
import com.carddemo.common.entity.Account;
import jakarta.persistence.EntityManagerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
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

/**
 * Spring Batch configuration for CBACT01C migration.
 * Reads accounts from DB, processes through business logic (edge cases),
 * and writes OUTFILE, ARRYFILE, and VBRCFILE.
 */
@Configuration
public class AccountReaderWriterJobConfig {

    @Value("${batch.output.dir:./output}")
    private String outputDir;

    @Value("${batch.chunk-size:10}")
    private int chunkSize;

    @Bean
    public JpaPagingItemReader<Account> accountReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<Account>()
                .name("accountReader")
                .entityManagerFactory(emf)
                .queryString("SELECT a FROM Account a ORDER BY a.acctId")
                .pageSize(chunkSize)
                .build();
    }

    @Bean
    public AccountItemProcessor accountProcessor() {
        return new AccountItemProcessor();
    }

    @Bean
    public AccountMultiFileWriter accountWriter() {
        Path dir = Paths.get(outputDir);
        return new AccountMultiFileWriter(
                dir.resolve("OUTFILE.dat"),
                dir.resolve("ARRYFILE.dat"),
                dir.resolve("VBRCFILE.dat"));
    }

    @Bean
    public Step accountReaderWriterStep(JobRepository jobRepository,
                                        PlatformTransactionManager txManager,
                                        JpaPagingItemReader<Account> accountReader,
                                        AccountItemProcessor accountProcessor,
                                        AccountMultiFileWriter accountWriter) {
        return new StepBuilder("accountReaderWriterStep", jobRepository)
                .<Account, AccountOutputBundle>chunk(chunkSize, txManager)
                .reader(accountReader)
                .processor(accountProcessor)
                .writer(accountWriter)
                .build();
    }

    @Bean
    public Job accountReaderWriterJob(JobRepository jobRepository, Step accountReaderWriterStep) {
        return new JobBuilder("accountReaderWriterJob", jobRepository)
                .start(accountReaderWriterStep)
                .build();
    }
}
