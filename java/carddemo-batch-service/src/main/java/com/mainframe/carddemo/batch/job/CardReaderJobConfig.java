package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.Card;
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
 * Replaces CBACT02C.cbl — reads card table and writes to a delimited flat file.
 */
@Configuration
public class CardReaderJobConfig {

    @Value("${batch.output.dir:./batch-output}")
    private String outputDir;

    @Bean
    public JpaPagingItemReader<Card> cardReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<Card>()
                .name("cardReader")
                .entityManagerFactory(emf)
                .queryString("SELECT c FROM Card c ORDER BY c.cardNum")
                .pageSize(100)
                .build();
    }

    @Bean
    public FlatFileItemWriter<Card> cardWriter() {
        return new FlatFileItemWriterBuilder<Card>()
                .name("cardWriter")
                .resource(new FileSystemResource(outputDir + "/cards.dat"))
                .delimited()
                .delimiter(",")
                .names("cardNum", "cardAcctId", "cardCvvCd", "cardEmbossedName",
                        "cardExpirationDate", "cardActiveStatus")
                .build();
    }

    @Bean
    public Step cardReaderStep(JobRepository jobRepository,
                               PlatformTransactionManager txManager,
                               JpaPagingItemReader<Card> cardReader,
                               FlatFileItemWriter<Card> cardWriter) {
        return new StepBuilder("cardReaderStep", jobRepository)
                .<Card, Card>chunk(100, txManager)
                .reader(cardReader)
                .writer(cardWriter)
                .build();
    }

    @Bean
    public Job cardReaderJob(JobRepository jobRepository, Step cardReaderStep) {
        return new JobBuilder("cardReaderJob", jobRepository)
                .start(cardReaderStep)
                .build();
    }
}
