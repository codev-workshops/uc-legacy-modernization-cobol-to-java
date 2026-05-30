package com.carddemo.batch.job.xrefprinter;

import com.carddemo.common.entity.CardXref;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
 * Spring Batch job that mirrors COBOL program CBACT03C: reads every row from
 * the {@code card_xref} table and writes a fixed-width report file matching
 * the CVACT03Y copybook layout.
 */
@Configuration
public class XrefPrinterJobConfig {

    static final String JOB_NAME = "xrefPrinterJob";
    static final String STEP_NAME = "xrefPrinterStep";
    static final String HEADER = "START OF EXECUTION OF PROGRAM CBACT03C";
    static final String FOOTER = "END OF EXECUTION OF PROGRAM CBACT03C";

    @Bean
    public JpaPagingItemReader<CardXref> xrefReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<CardXref>()
                .name("xrefReader")
                .entityManagerFactory(emf)
                .queryString("SELECT c FROM CardXref c ORDER BY c.xrefCardNum")
                .pageSize(100)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CardXref> xrefWriter(
            @Value("#{jobParameters['outputPath'] ?: 'xref-report.txt'}") String outputPath) {
        return new FlatFileItemWriterBuilder<CardXref>()
                .name("xrefWriter")
                .resource(new FileSystemResource(outputPath))
                .lineAggregator(new XrefLineAggregator())
                .headerCallback(writer -> writer.write(HEADER))
                .footerCallback(writer -> writer.write(FOOTER))
                .build();
    }

    @Bean
    public Step xrefPrinterStep(JobRepository jobRepository,
                                PlatformTransactionManager txManager,
                                JpaPagingItemReader<CardXref> xrefReader,
                                FlatFileItemWriter<CardXref> xrefWriter) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<CardXref, CardXref>chunk(100, txManager)
                .reader(xrefReader)
                .writer(xrefWriter)
                .build();
    }

    @Bean
    public Job xrefPrinterJob(JobRepository jobRepository, Step xrefPrinterStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(xrefPrinterStep)
                .build();
    }
}
