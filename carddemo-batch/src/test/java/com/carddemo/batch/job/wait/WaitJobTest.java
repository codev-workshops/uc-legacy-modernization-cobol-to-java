package com.carddemo.batch.job.wait;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.carddemo.batch.TestBatchApplication;

@SpringBootTest(classes = TestBatchApplication.class)
@SpringBatchTest
class WaitJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job waitJob;

    // --- valid input --------------------------------------------------------

    @Test
    void completesWithValidWaitTime() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("waitTime", 1L) // 1 centisecond = 10 ms
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

    @Test
    void sleepsDurationMatchesCentiseconds() throws Exception {
        long centiseconds = 5L; // 50 ms
        JobParameters params = new JobParametersBuilder()
                .addLong("waitTime", centiseconds)
                .toJobParameters();

        long start = System.currentTimeMillis();
        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(elapsed).isGreaterThanOrEqualTo(centiseconds * 10 - 5);
    }

    // --- invalid input: null ------------------------------------------------

    @Test
    void failsWhenWaitTimeIsNull() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(execution.getAllFailureExceptions())
                .anySatisfy(ex -> assertThat(ex.getMessage())
                        .contains("waitTime"));
    }

    // --- invalid input: negative -------------------------------------------

    @Test
    void failsWhenWaitTimeIsNegative() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("waitTime", -100L)
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(execution.getAllFailureExceptions())
                .anySatisfy(ex -> assertThat(ex.getMessage())
                        .contains("positive"));
    }

    // --- invalid input: zero -----------------------------------------------

    @Test
    void failsWhenWaitTimeIsZero() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("waitTime", 0L)
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(execution.getAllFailureExceptions())
                .anySatisfy(ex -> assertThat(ex.getMessage())
                        .contains("positive"));
    }
}
