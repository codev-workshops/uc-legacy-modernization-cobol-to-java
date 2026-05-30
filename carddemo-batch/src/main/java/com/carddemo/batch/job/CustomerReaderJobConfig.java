package com.carddemo.batch.job;

import com.carddemo.batch.writer.CustomerRecordFormatter;
import com.carddemo.common.entity.Customer;

import jakarta.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CustomerReaderJobConfig {

    private static final Logger log = LoggerFactory.getLogger(CustomerReaderJobConfig.class);
    private static final int CHUNK_SIZE = 10;
    static final String HEADER = "START OF EXECUTION OF PROGRAM CBCUS01C";
    static final String FOOTER = "END OF EXECUTION OF PROGRAM CBCUS01C";

    @Value("${batch.customer-report.output-path:customer-report.txt}")
    private String outputPath;

    @Bean
    public JpaPagingItemReader<Customer> customerItemReader(
            EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Customer c ORDER BY c.custId")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public FlatFileItemWriter<Customer> customerReportWriter() {
        WritableResource resource = new FileSystemResource(outputPath);
        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerReportWriter")
                .resource(resource)
                .lineAggregator(CustomerRecordFormatter::format)
                .headerCallback(headerCallback())
                .footerCallback(footerCallback())
                .build();
    }

    @Bean
    public FlatFileHeaderCallback headerCallback() {
        return writer -> writer.write(HEADER);
    }

    @Bean
    public FlatFileFooterCallback footerCallback() {
        return writer -> writer.write(FOOTER);
    }

    @Bean
    public Step customerReaderStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaPagingItemReader<Customer> customerItemReader,
            FlatFileItemWriter<Customer> customerReportWriter) {
        return new StepBuilder("customerReaderStep", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
                .reader(customerItemReader)
                .writer(customerReportWriter)
                .build();
    }

    @Bean
    public Job customerReaderJob(
            JobRepository jobRepository,
            Step customerReaderStep) {
        return new JobBuilder("customerReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(customerReaderStep)
                .build();
    }
}
