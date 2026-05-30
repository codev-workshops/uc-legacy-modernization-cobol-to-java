package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.Customer;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Replaces CBCUS01C.cbl — reads customer table and writes to a delimited flat file.
 */
@Configuration
public class CustomerReaderJobConfig {

    @Value("${batch.output.dir:./batch-output}")
    private String outputDir;

    @Bean
    public JpaPagingItemReader<Customer> customerReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("customerReader")
                .entityManagerFactory(emf)
                .queryString("SELECT c FROM Customer c ORDER BY c.custId")
                .pageSize(100)
                .build();
    }

    @Bean
    public FlatFileItemWriter<Customer> customerWriter() {
        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerWriter")
                .resource(new FileSystemResource(outputDir + "/customers.dat"))
                .delimited()
                .delimiter(",")
                .names("custId", "custFirstName", "custMiddleName", "custLastName",
                        "custAddrLine1", "custAddrLine2", "custAddrLine3",
                        "custAddrStateCd", "custAddrCountryCd", "custAddrZip",
                        "custPhoneNum1", "custPhoneNum2", "custSsn",
                        "custGovtIssuedId", "custDob", "custEftAccountId",
                        "custPriCardHolderInd", "custFicoCreditScore")
                .build();
    }

    @Bean
    public Step customerReaderStep(JobRepository jobRepository,
                                   PlatformTransactionManager txManager,
                                   JpaPagingItemReader<Customer> customerReader,
                                   FlatFileItemWriter<Customer> customerWriter) {
        return new StepBuilder("customerReaderStep", jobRepository)
                .<Customer, Customer>chunk(100, txManager)
                .reader(customerReader)
                .writer(customerWriter)
                .build();
    }

    @Bean
    public Job customerReaderJob(JobRepository jobRepository, Step customerReaderStep) {
        return new JobBuilder("customerReaderJob", jobRepository)
                .start(customerReaderStep)
                .build();
    }
}
