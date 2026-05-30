package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.CardXref;
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
 * Replaces CBACT03C.cbl — reads card_xref table and writes to a delimited flat file.
 */
@Configuration
public class XrefReaderJobConfig {

    @Value("${batch.output.dir:./batch-output}")
    private String outputDir;

    @Bean
    public JpaPagingItemReader<CardXref> xrefReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<CardXref>()
                .name("xrefReader")
                .entityManagerFactory(emf)
                .queryString("SELECT x FROM CardXref x ORDER BY x.xrefCardNum")
                .pageSize(100)
                .build();
    }

    @Bean
    public FlatFileItemWriter<CardXref> xrefWriter() {
        return new FlatFileItemWriterBuilder<CardXref>()
                .name("xrefWriter")
                .resource(new FileSystemResource(outputDir + "/card_xref.dat"))
                .delimited()
                .delimiter(",")
                .names("xrefCardNum", "xrefCustId", "xrefAcctId")
                .build();
    }

    @Bean
    public Step xrefReaderStep(JobRepository jobRepository,
                               PlatformTransactionManager txManager,
                               JpaPagingItemReader<CardXref> xrefReader,
                               FlatFileItemWriter<CardXref> xrefWriter) {
        return new StepBuilder("xrefReaderStep", jobRepository)
                .<CardXref, CardXref>chunk(100, txManager)
                .reader(xrefReader)
                .writer(xrefWriter)
                .build();
    }

    @Bean
    public Job xrefReaderJob(JobRepository jobRepository, Step xrefReaderStep) {
        return new JobBuilder("xrefReaderJob", jobRepository)
                .start(xrefReaderStep)
                .build();
    }
}
