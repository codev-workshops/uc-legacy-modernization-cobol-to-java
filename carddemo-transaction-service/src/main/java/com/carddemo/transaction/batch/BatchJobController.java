package com.carddemo.transaction.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchJobController {

    private final JobLauncher jobLauncher;

    @Qualifier("validationJob")
    private final Job validationJob;

    @Qualifier("postingJob")
    private final Job postingJob;

    @Qualifier("interestJob")
    private final Job interestJob;

    @Qualifier("reportJob")
    private final Job reportJob;

    @Qualifier("statementJob")
    private final Job statementJob;

    @PostMapping("/validate")
    public ResponseEntity<Map<String, String>> runValidation() {
        return launchJob("transactionValidation", validationJob);
    }

    @PostMapping("/post")
    public ResponseEntity<Map<String, String>> runPosting() {
        return launchJob("transactionPosting", postingJob);
    }

    @PostMapping("/interest")
    public ResponseEntity<Map<String, String>> runInterest() {
        return launchJob("interestCalculation", interestJob);
    }

    @PostMapping("/report")
    public ResponseEntity<Map<String, String>> runReport() {
        return launchJob("transactionReport", reportJob);
    }

    @PostMapping("/statement")
    public ResponseEntity<Map<String, String>> runStatement() {
        return launchJob("statementGeneration", statementJob);
    }

    private ResponseEntity<Map<String, String>> launchJob(String name, Job job) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, params);
            log.info("Job {} launched successfully", name);
            return ResponseEntity.ok(Map.of("status", "STARTED", "job", name));
        } catch (Exception e) {
            log.error("Failed to launch job {}: {}", name, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "FAILED", "job", name, "error", e.getMessage()));
        }
    }
}
