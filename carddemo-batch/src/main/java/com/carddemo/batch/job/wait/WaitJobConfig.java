package com.carddemo.batch.job.wait;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job equivalent of COBSWAIT.cbl.
 *
 * <p>Defines a single-step job that sleeps for a configurable number of
 * centiseconds (1/100 s), matching the COBOL MVSWAIT interface.</p>
 */
@Configuration
public class WaitJobConfig {

    @Bean
    public Job waitJob(JobRepository jobRepository, Step waitStep) {
        return new JobBuilder("waitJob", jobRepository)
                .start(waitStep)
                .build();
    }

    @Bean
    public Step waitStep(JobRepository jobRepository,
                         PlatformTransactionManager transactionManager) {
        return new StepBuilder("waitStep", jobRepository)
                .tasklet(waitTasklet(), transactionManager)
                .build();
    }

    @Bean
    public WaitTasklet waitTasklet() {
        return new WaitTasklet();
    }
}
