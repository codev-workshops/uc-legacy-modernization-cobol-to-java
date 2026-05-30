package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.Account;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Replaces CBACT01C.cbl — reads account table and writes to a delimited flat file.
 */
@Configuration
public class AccountReaderJobConfig {

    @Value("${batch.output.dir:./batch-output}")
    private String outputDir;

    @Bean
    public JpaPagingItemReader<Account> accountReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<Account>()
                .name("accountReader")
                .entityManagerFactory(emf)
                .queryString("SELECT a FROM Account a ORDER BY a.acctId")
                .pageSize(100)
                .build();
    }

    @Bean
    public FlatFileItemWriter<Account> accountWriter() {
        return new FlatFileItemWriterBuilder<Account>()
                .name("accountWriter")
                .resource(new FileSystemResource(outputDir + "/accounts.dat"))
                .delimited()
                .delimiter(",")
                .names("acctId", "acctActiveStatus", "acctCurrBal", "acctCreditLimit",
                        "acctCashCreditLimit", "acctOpenDate", "acctExpirationDate",
                        "acctReissueDate", "acctCurrCycCredit", "acctCurrCycDebit",
                        "acctAddrZip", "acctGroupId")
                .build();
    }

    @Bean
    public Step accountReaderStep(JobRepository jobRepository,
                                  PlatformTransactionManager txManager,
                                  JpaPagingItemReader<Account> accountReader,
                                  FlatFileItemWriter<Account> accountWriter) {
        return new StepBuilder("accountReaderStep", jobRepository)
                .<Account, Account>chunk(100, txManager)
                .reader(accountReader)
                .writer(accountWriter)
                .build();
    }

    @Bean
    public Job accountReaderJob(JobRepository jobRepository, Step accountReaderStep) {
        return new JobBuilder("accountReaderJob", jobRepository)
                .start(accountReaderStep)
                .build();
    }
}
