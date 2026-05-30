package com.carddemo.batch.cbact01c.config;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.service.AccountFileProcessor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Optional Spring Batch job configuration for CBACT01C.
 * Wires a FlatFileItemReader for the input account file and delegates
 * processing to {@link AccountFileProcessor}.
 *
 * This class demonstrates the recommended Spring Batch approach per
 * MODERNIZATION_BLUEPRINT.md. For production use, the writer should be
 * replaced with a CompositeItemWriter writing to 3 output files.
 */
@Configuration
public class Cbact01cJobConfig {

    @Value("${cbact01c.input.file:classpath:sample-accounts.dat}")
    private Resource inputFile;

    @Bean
    public FlatFileItemReader<AccountRecord> accountReader() {
        return new FlatFileItemReaderBuilder<AccountRecord>()
                .name("accountReader")
                .resource(inputFile)
                .delimited()
                .delimiter("|")
                .names("acctId", "activeStatus", "currBal", "creditLimit",
                       "cashCreditLimit", "openDate", "expirationDate",
                       "reissueDate", "currCycCredit", "currCycDebit",
                       "addrZip", "groupId")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(AccountRecord.class);
                }})
                .build();
    }

    @Bean
    public ItemProcessor<AccountRecord, OutAccountRecord> accountProcessor(
            AccountFileProcessor processor) {
        return processor::buildOutRecord;
    }

    @Bean
    public Job cbact01cJob(JobRepository jobRepository, Step processAccountsStep) {
        return new JobBuilder("cbact01cJob", jobRepository)
                .start(processAccountsStep)
                .build();
    }

    @Bean
    public Step processAccountsStep(JobRepository jobRepository,
                                    PlatformTransactionManager txManager,
                                    FlatFileItemReader<AccountRecord> reader,
                                    ItemProcessor<AccountRecord, OutAccountRecord> processor) {
        return new StepBuilder("processAccountsStep", jobRepository)
                .<AccountRecord, OutAccountRecord>chunk(10, txManager)
                .reader(reader)
                .processor(processor)
                .writer(items -> {
                    // Placeholder: in production, replace with CompositeItemWriter
                    // writing to OUTFILE, ARRYFILE, and VBRCFILE equivalents
                })
                .build();
    }
}
