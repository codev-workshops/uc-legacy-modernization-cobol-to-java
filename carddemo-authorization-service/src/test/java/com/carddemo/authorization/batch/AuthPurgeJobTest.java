package com.carddemo.authorization.batch;

import com.carddemo.authorization.service.AuthPurgeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class AuthPurgeJobTest {

    @Mock
    private AuthPurgeService authPurgeService;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    @Test
    void authPurgeJobBean_createsJob() {
        AuthPurgeJob purgeJob = new AuthPurgeJob(authPurgeService);
        Step step = purgeJob.authPurgeStep(jobRepository, transactionManager);
        assertNotNull(step);

        Job job = purgeJob.authPurgeJobBean(jobRepository, step);
        assertNotNull(job);
        assertNotNull(job.getName());
    }

    @Test
    void authPurgeTasklet_createsTasklet() {
        AuthPurgeJob purgeJob = new AuthPurgeJob(authPurgeService);
        assertNotNull(purgeJob.authPurgeTasklet());
    }
}
