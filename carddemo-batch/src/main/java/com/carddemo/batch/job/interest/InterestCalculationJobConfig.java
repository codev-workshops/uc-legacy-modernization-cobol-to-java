package com.carddemo.batch.job.interest;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch configuration for the Interest Calculation job.
 * Mirrors COBOL program CBACT04C (INTCALC): monthly interest
 * calculation and balance update for all accounts.
 */
@Configuration
public class InterestCalculationJobConfig {

    @Bean
    public Step interestCalculationStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        InterestCalculationTasklet tasklet) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job interestCalculationJob(JobRepository jobRepository,
                                      Step interestCalculationStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep)
                .build();
    }
}
