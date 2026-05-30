package com.mainframe.carddemo.report.controller;

import com.mainframe.carddemo.report.entity.GeneratedReport;
import com.mainframe.carddemo.report.repository.GeneratedReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobLauncher jobLauncher;

    @MockBean(name = "statementGenerationJob")
    private Job statementGenerationJob;

    @MockBean
    private GeneratedReportRepository reportRepository;

    private GeneratedReport sampleReport;

    @BeforeEach
    void setUp() {
        sampleReport = new GeneratedReport();
        sampleReport.setId(1L);
        sampleReport.setAccountId(12345L);
        sampleReport.setCustomerName("John Doe");
        sampleReport.setReportType("STATEMENT");
        sampleReport.setTextContent("Sample text statement");
        sampleReport.setHtmlContent("<html>Sample</html>");
        sampleReport.setGeneratedAt(LocalDateTime.of(2024, 6, 15, 10, 0));
    }

    @Test
    void shouldListReports() throws Exception {
        when(reportRepository.findAll()).thenReturn(List.of(sampleReport));

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountId").value(12345))
                .andExpect(jsonPath("$[0].customerName").value("John Doe"))
                .andExpect(jsonPath("$[0].reportType").value("STATEMENT"));
    }

    @Test
    void shouldGetReportById() throws Exception {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));

        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.textContent").value("Sample text statement"));
    }

    @Test
    void shouldReturn404ForMissingReport() throws Exception {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reports/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldTriggerReportGeneration() throws Exception {
        mockMvc.perform(post("/api/reports/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
