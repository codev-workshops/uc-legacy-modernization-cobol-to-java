package com.carddemo.online.service;

import com.carddemo.online.dto.ReportRequest;
import com.carddemo.online.dto.ReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job transactionReportJob;

    @Mock
    private JobExplorer jobExplorer;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(jobLauncher, transactionReportJob, jobExplorer);
    }

    @Test
    void generateReport_success() throws Exception {
        ReportRequest request = new ReportRequest("2024-01-01", "2024-01-31");

        JobExecution execution = new JobExecution(42L);
        execution.setStatus(BatchStatus.STARTING);
        when(jobLauncher.run(eq(transactionReportJob), any(JobParameters.class)))
                .thenReturn(execution);

        ReportResponse response = reportService.generateReport(request);

        assertEquals(42L, response.getExecutionId());
        assertEquals("STARTING", response.getStatus());
        assertNotNull(response.getOutputPath());
        assertTrue(response.getOutputPath().contains("transaction_report_20240101_20240131"));
    }

    @Test
    void generateReport_jobLauncherFails_throwsException() throws Exception {
        ReportRequest request = new ReportRequest("2024-01-01", "2024-01-31");

        when(jobLauncher.run(eq(transactionReportJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Launch failed"));

        assertThrows(ReportService.ReportGenerationException.class,
                () -> reportService.generateReport(request));
    }

    @Test
    void getReport_exists_returnsResponse() {
        JobParameters params = new JobParametersBuilder()
                .addString("outputPath", "/tmp/report.txt")
                .toJobParameters();
        JobExecution execution = new JobExecution(1L, params);
        execution.setStatus(BatchStatus.COMPLETED);
        when(jobExplorer.getJobExecution(1L)).thenReturn(execution);

        ReportResponse response = reportService.getReport(1L);

        assertEquals(1L, response.getExecutionId());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals("/tmp/report.txt", response.getOutputPath());
    }

    @Test
    void getReport_notFound_throwsException() {
        when(jobExplorer.getJobExecution(999L)).thenReturn(null);

        assertThrows(ReportService.ReportNotFoundException.class,
                () -> reportService.getReport(999L));
    }

    @Test
    void buildOutputPath_formatsCorrectly() {
        String path = reportService.buildOutputPath("2024-03-15", "2024-03-31");
        assertTrue(path.contains("transaction_report_20240315_20240331.txt"));
    }
}
