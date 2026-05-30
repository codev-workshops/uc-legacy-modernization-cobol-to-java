package com.carddemo.authorization.batch;

import com.carddemo.authorization.service.AuthPurgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AuthPurgeJob {

    private static final Logger log = LoggerFactory.getLogger(AuthPurgeJob.class);

    private final AuthPurgeService authPurgeService;

    public AuthPurgeJob(AuthPurgeService authPurgeService) {
        this.authPurgeService = authPurgeService;
    }

    @Bean
    public Job authPurgeJobBean(JobRepository jobRepository, Step authPurgeStep) {
        return new JobBuilder("authPurgeJob", jobRepository)
                .start(authPurgeStep)
                .build();
    }

    @Bean
    public Step authPurgeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("authPurgeStep", jobRepository)
                .tasklet(authPurgeTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet authPurgeTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Starting authorization purge job");
            int purged = authPurgeService.purgeExpiredAuthorizations();
            log.info("Authorization purge job completed. Purged {} records", purged);
            return RepeatStatus.FINISHED;
        };
    }
}
