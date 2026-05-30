package com.carddemo.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/batch")
@Tag(name = "Batch Jobs", description = "Endpoints for launching batch reader, import, and export jobs")
public class BatchController {

    private static final Logger log = LoggerFactory.getLogger(BatchController.class);

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobs;

    public BatchController(JobLauncher jobLauncher, Map<String, Job> jobs) {
        this.jobLauncher = jobLauncher;
        this.jobs = jobs;
    }

    @PostMapping("/jobs/{jobName}")
    @Operation(summary = "Launch a batch job", description = "Launches the specified batch reader job by name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job launched successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "500", description = "Job failed to launch")
    })
    public ResponseEntity<Map<String, Object>> launchJob(
            @Parameter(description = "Name of the batch job to launch")
            @PathVariable String jobName) {
        Job job = jobs.get(jobName);
        if (job == null) {
            log.warn("Job not found: {}", jobName);
            return ResponseEntity.notFound().build();
        }

        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, params);
            log.info("Job '{}' launched with execution ID: {}", jobName, execution.getId());

            return ResponseEntity.ok(Map.of(
                    "jobExecutionId", execution.getId(),
                    "jobName", jobName,
                    "status", execution.getStatus().toString()
            ));
        } catch (Exception e) {
            log.error("Failed to launch job '{}': {}", jobName, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "jobName", jobName,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/jobs/import")
    @Operation(summary = "Launch data import job",
            description = "Launches the data import job (CBIMPORT) to read flat files and populate database tables")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import job launched successfully"),
            @ApiResponse(responseCode = "500", description = "Import job failed to launch")
    })
    public ResponseEntity<Map<String, Object>> launchImportJob(
            @Parameter(description = "Path to the input file or directory containing data files")
            @RequestParam String inputFile) {
        Job job = jobs.get("dataImportJob");
        if (job == null) {
            log.error("dataImportJob not found in job registry");
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "dataImportJob not configured"
            ));
        }

        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("inputFile", inputFile)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, params);
            log.info("Import job launched with execution ID: {}, inputFile: {}",
                    execution.getId(), inputFile);

            return ResponseEntity.ok(Map.of(
                    "jobExecutionId", execution.getId(),
                    "jobName", "dataImportJob",
                    "status", execution.getStatus().toString(),
                    "inputFile", inputFile
            ));
        } catch (Exception e) {
            log.error("Failed to launch import job: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "jobName", "dataImportJob",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/jobs/export")
    @Operation(summary = "Launch data export job",
            description = "Launches the data export job (CBEXPORT) to export data as a multi-record-type flat file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Export job launched successfully"),
            @ApiResponse(responseCode = "500", description = "Export job failed to launch")
    })
    public ResponseEntity<Map<String, Object>> launchExportJob(
            @Parameter(description = "Path to the output export file")
            @RequestParam String outputFile) {
        Job job = jobs.get("dataExportJob");
        if (job == null) {
            log.error("dataExportJob not found in job registry");
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "dataExportJob not configured"
            ));
        }

        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("outputFile", outputFile)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, params);
            log.info("Export job launched with execution ID: {}, outputFile: {}",
                    execution.getId(), outputFile);

            return ResponseEntity.ok(Map.of(
                    "jobExecutionId", execution.getId(),
                    "jobName", "dataExportJob",
                    "status", execution.getStatus().toString(),
                    "outputFile", outputFile
            ));
        } catch (Exception e) {
            log.error("Failed to launch export job: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "jobName", "dataExportJob",
                    "error", e.getMessage()
            ));
        }
    }
}
