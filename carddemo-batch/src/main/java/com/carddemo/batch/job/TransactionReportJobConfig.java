package com.carddemo.batch.job;

import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TranCategoryRepository;
import com.carddemo.common.repository.TranTypeRepository;
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

import java.util.Map;

@Configuration
public class TransactionReportJobConfig {

    @Bean
    public Job transactionReportJob(JobRepository jobRepository, Step transactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(transactionReportStep)
                .build();
    }

    @Bean
    public Step transactionReportStep(JobRepository jobRepository,
                                      PlatformTransactionManager txManager,
                                      JpaCursorItemReader<Transaction> transactionReader,
                                      TransactionReportProcessor reportProcessor,
                                      TransactionReportWriter reportWriter) {
        return new StepBuilder("transactionReportStep", jobRepository)
                .<Transaction, TransactionReportItem>chunk(100, txManager)
                .reader(transactionReader)
                .processor(reportProcessor)
                .writer(reportWriter)
                .listener(reportWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Transaction> transactionReader(
            EntityManagerFactory emf,
            @Value("#{jobParameters['startDate']}") String startDate,
            @Value("#{jobParameters['endDate']}") String endDate) {

        JpaCursorItemReader<Transaction> reader = new JpaCursorItemReader<>();
        reader.setEntityManagerFactory(emf);
        reader.setQueryString(
                "SELECT t FROM Transaction t "
                + "WHERE SUBSTRING(t.procTs, 1, 10) >= :startDate "
                + "AND SUBSTRING(t.procTs, 1, 10) <= :endDate "
                + "ORDER BY t.cardNum, t.procTs");
        reader.setParameterValues(Map.of("startDate", startDate, "endDate", endDate));
        return reader;
    }

    @Bean
    public TransactionReportProcessor reportProcessor(CardXrefRepository cardXrefRepo,
                                                      TranTypeRepository tranTypeRepo,
                                                      TranCategoryRepository tranCatRepo) {
        return new TransactionReportProcessor(cardXrefRepo, tranTypeRepo, tranCatRepo);
    }

    @Bean
    @StepScope
    public TransactionReportWriter reportWriter(
            @Value("#{jobParameters['outputPath']}") String outputPath,
            @Value("#{jobParameters['startDate']}") String startDate,
            @Value("#{jobParameters['endDate']}") String endDate) {
        return new TransactionReportWriter(outputPath, startDate, endDate);
    }
}
