package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.DailyTransaction;
import com.mainframe.carddemo.batch.repository.BatchTransactionRepository;
import com.mainframe.carddemo.batch.repository.DailyRejectRepository;
import com.mainframe.carddemo.batch.repository.TranCatBalanceRepository;
import com.mainframe.carddemo.common.client.AccountServiceClient;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
public class TransactionPostingJobConfig {

    @Bean
    public JpaPagingItemReader<DailyTransaction> dailyTransactionReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<DailyTransaction>()
                .name("dailyTransactionReader")
                .entityManagerFactory(emf)
                .queryString("SELECT d FROM DailyTransaction d ORDER BY d.tranId")
                .pageSize(100)
                .build();
    }

    @Bean
    public XrefValidationProcessor xrefValidationProcessor(AccountServiceClient accountServiceClient) {
        return new XrefValidationProcessor(accountServiceClient);
    }

    @Bean
    public AccountValidationProcessor accountValidationProcessor(AccountServiceClient accountServiceClient) {
        return new AccountValidationProcessor(accountServiceClient);
    }

    @Bean
    public CreditLimitProcessor creditLimitProcessor() {
        return new CreditLimitProcessor();
    }

    @Bean
    public ExpirationProcessor expirationProcessor() {
        return new ExpirationProcessor();
    }

    @Bean
    public CompositeItemProcessor<DailyTransaction, PostingResult> postingCompositeProcessor(
            XrefValidationProcessor xrefValidationProcessor,
            AccountValidationProcessor accountValidationProcessor,
            CreditLimitProcessor creditLimitProcessor,
            ExpirationProcessor expirationProcessor) {

        CompositeItemProcessor<DailyTransaction, PostingResult> processor = new CompositeItemProcessor<>();
        processor.setDelegates(List.of(
                xrefValidationProcessor,
                accountValidationProcessor,
                creditLimitProcessor,
                expirationProcessor
        ));
        return processor;
    }

    @Bean
    public PostingItemWriter postingItemWriter(BatchTransactionRepository transactionRepository,
                                               DailyRejectRepository rejectRepository,
                                               TranCatBalanceRepository tranCatBalanceRepository,
                                               AccountServiceClient accountServiceClient) {
        return new PostingItemWriter(transactionRepository, rejectRepository,
                tranCatBalanceRepository, accountServiceClient);
    }

    @Bean
    public Step transactionPostingStep(JobRepository jobRepository,
                                       PlatformTransactionManager txManager,
                                       JpaPagingItemReader<DailyTransaction> dailyTransactionReader,
                                       CompositeItemProcessor<DailyTransaction, PostingResult> postingCompositeProcessor,
                                       PostingItemWriter postingItemWriter) {
        return new StepBuilder("transactionPostingStep", jobRepository)
                .<DailyTransaction, PostingResult>chunk(100, txManager)
                .reader(dailyTransactionReader)
                .processor(postingCompositeProcessor)
                .writer(postingItemWriter)
                .build();
    }

    @Bean
    public Job transactionPostingJob(JobRepository jobRepository, Step transactionPostingStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(transactionPostingStep)
                .build();
    }
}
