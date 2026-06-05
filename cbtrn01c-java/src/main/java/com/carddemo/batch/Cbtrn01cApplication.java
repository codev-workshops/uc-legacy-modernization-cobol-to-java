package com.carddemo.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot application entry point for CBTRN01C batch job.
 * Mirrors the COBOL program's execution lifecycle:
 * - Displays START/END messages
 * - Exits with code 999 on failure (matching COBOL's CEE3ABD abend)
 */
@SpringBootApplication
public class Cbtrn01cApplication {

    private static final Logger log = LoggerFactory.getLogger(Cbtrn01cApplication.class);
    private static final int ABEND_EXIT_CODE = 999;

    public static void main(String[] args) {
        log.info("START OF EXECUTION OF PROGRAM CBTRN01C");
        try {
            SpringApplication.run(Cbtrn01cApplication.class, args);
        } catch (Exception e) {
            log.error("ABEND: {}", e.getMessage(), e);
            System.exit(ABEND_EXIT_CODE);
        }
    }

    @Bean
    public CommandLineRunner jobRunner(JobLauncher jobLauncher, Job validateDailyTransactionsJob) {
        return args -> {
            JobExecution execution = jobLauncher.run(validateDailyTransactionsJob, new JobParameters());

            if (execution.getStatus() == BatchStatus.COMPLETED) {
                log.info("END OF EXECUTION OF PROGRAM CBTRN01C");
            } else {
                log.error("JOB FAILED WITH STATUS: {}", execution.getStatus());
                System.exit(ABEND_EXIT_CODE);
            }
        };
    }
}
