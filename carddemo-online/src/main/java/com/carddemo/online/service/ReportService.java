package com.carddemo.online.service;

import com.carddemo.online.dto.ReportRequest;
import com.carddemo.online.dto.ReportResponse;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ReportService {

    private final JobLauncher jobLauncher;
    private final Job transactionReportJob;
    private final JobExplorer jobExplorer;

    public ReportService(
            @org.springframework.beans.factory.annotation.Qualifier("asyncJobLauncher")
            JobLauncher jobLauncher,
            Job transactionReportJob,
            JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.transactionReportJob = transactionReportJob;
        this.jobExplorer = jobExplorer;
    }

    public ReportResponse generateReport(ReportRequest request) {
        try {
            String outputPath = buildOutputPath(request.getStartDate(), request.getEndDate());

            JobParameters params = new JobParametersBuilder()
                    .addString("startDate", request.getStartDate())
                    .addString("endDate", request.getEndDate())
                    .addString("outputPath", outputPath)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(transactionReportJob, params);

            return new ReportResponse(
                    execution.getId(),
                    execution.getStatus().name(),
                    outputPath);
        } catch (Exception e) {
            throw new ReportGenerationException(
                    "Failed to launch report job: " + e.getMessage(), e);
        }
    }

    public ReportResponse getReport(Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            throw new ReportNotFoundException("Report execution not found: " + executionId);
        }

        String outputPath = execution.getJobParameters().getString("outputPath");
        return new ReportResponse(
                execution.getId(),
                execution.getStatus().name(),
                outputPath);
    }

    String buildOutputPath(String startDate, String endDate) {
        String safeStart = startDate.replace("-", "");
        String safeEnd = endDate.replace("-", "");
        String filename = String.format("transaction_report_%s_%s.txt", safeStart, safeEnd);
        return Paths.get(System.getProperty("java.io.tmpdir"), filename).toString();
    }

    public static class ReportGenerationException extends RuntimeException {
        public ReportGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ReportNotFoundException extends RuntimeException {
        public ReportNotFoundException(String message) {
            super(message);
        }
    }
}
