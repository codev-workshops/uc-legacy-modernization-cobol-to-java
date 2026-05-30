package com.carddemo.batch.job;

import com.carddemo.common.entity.Card;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CardDataPrinterJobConfig {

    @Value("${carddemo.batch.output-file:card-data-report.txt}")
    private String outputFilePath;

    @Bean
    public JpaCursorItemReader<Card> cardItemReader(EntityManagerFactory entityManagerFactory) {
        return new JpaCursorItemReaderBuilder<Card>()
                .name("cardItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Card c ORDER BY c.cardNum")
                .build();
    }

    @Bean
    public CardRecordLineAggregator cardRecordLineAggregator() {
        return new CardRecordLineAggregator();
    }

    @Bean
    public FlatFileItemWriter<Card> cardReportWriter(CardRecordLineAggregator aggregator) {
        WritableResource resource = new FileSystemResource(outputFilePath);
        return new FlatFileItemWriterBuilder<Card>()
                .name("cardReportWriter")
                .resource(resource)
                .lineAggregator(aggregator)
                .headerCallback(writer -> writer.write("START OF EXECUTION OF PROGRAM CBACT02C"))
                .footerCallback(writer -> writer.write("END OF EXECUTION OF PROGRAM CBACT02C"))
                .build();
    }

    @Bean
    public Step cardDataPrinterStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    JpaCursorItemReader<Card> cardItemReader,
                                    FlatFileItemWriter<Card> cardReportWriter) {
        return new StepBuilder("cardDataPrinterStep", jobRepository)
                .<Card, Card>chunk(10, transactionManager)
                .reader(cardItemReader)
                .writer(cardReportWriter)
                .build();
    }

    @Bean
    public Job cardDataPrinterJob(JobRepository jobRepository, Step cardDataPrinterStep) {
        return new JobBuilder("cardDataPrinterJob", jobRepository)
                .start(cardDataPrinterStep)
                .build();
    }
}
