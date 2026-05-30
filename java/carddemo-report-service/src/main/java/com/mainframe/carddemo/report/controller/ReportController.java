package com.mainframe.carddemo.report.controller;

import com.mainframe.carddemo.report.entity.GeneratedReport;
import com.mainframe.carddemo.report.repository.GeneratedReportRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final JobLauncher jobLauncher;
    private final Job statementGenerationJob;
    private final GeneratedReportRepository reportRepository;

    public ReportController(JobLauncher jobLauncher,
                            @Qualifier("statementGenerationJob") Job statementGenerationJob,
                            GeneratedReportRepository reportRepository) {
        this.jobLauncher = jobLauncher;
        this.statementGenerationJob = statementGenerationJob;
        this.reportRepository = reportRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateReports() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(statementGenerationJob, params);
            return ResponseEntity.ok(Map.of("status", "COMPLETED"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "FAILED", "error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<GeneratedReport>> listReports() {
        return ResponseEntity.ok(reportRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneratedReport> getReport(@PathVariable Long id) {
        return reportRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
