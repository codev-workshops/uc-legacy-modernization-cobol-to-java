package com.carddemo.transaction.batch;

import com.carddemo.transaction.entity.TransactionType;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TransactionTypeBatchJob {

    private final TransactionTypeRepository transactionTypeRepository;

    @Value("${carddemo.batch.output-dir:output}")
    private String outputDir;

    @Bean
    public Job transactionTypeUpdateJob(JobRepository jobRepository,
                                        Step transactionTypeUpdateStep) {
        return new JobBuilder("transactionTypeUpdateJob", jobRepository)
                .start(transactionTypeUpdateStep)
                .build();
    }

    @Bean
    public Step transactionTypeUpdateStep(JobRepository jobRepository,
                                          PlatformTransactionManager transactionManager) {
        return new StepBuilder("transactionTypeUpdateStep", jobRepository)
                .<TransactionType, TransactionType>chunk(10, transactionManager)
                .reader(transactionTypeReader())
                .processor(transactionTypeProcessor())
                .writer(transactionTypeWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<TransactionType> transactionTypeReader() {
        BeanWrapperFieldSetMapper<TransactionType> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(TransactionType.class);

        return new FlatFileItemReaderBuilder<TransactionType>()
                .name("transactionTypeReader")
                .resource(new PathResource(outputDir + "/transaction_types.dat"))
                .delimited()
                .delimiter(",")
                .names("tranType", "tranTypeDesc")
                .fieldSetMapper(mapper)
                .build();
    }

    @Bean
    public ItemProcessor<TransactionType, TransactionType> transactionTypeProcessor() {
        return item -> {
            log.debug("Processing transaction type: {}", item.getTranType());
            if (item.getCreatedAt() == null) {
                item.setCreatedAt(LocalDateTime.now());
            }
            return item;
        };
    }

    @Bean
    public ItemWriter<TransactionType> transactionTypeWriter() {
        return items -> {
            for (TransactionType item : items) {
                transactionTypeRepository.save(item);
                log.info("Saved transaction type: {}", item.getTranType());
            }
        };
    }
}
