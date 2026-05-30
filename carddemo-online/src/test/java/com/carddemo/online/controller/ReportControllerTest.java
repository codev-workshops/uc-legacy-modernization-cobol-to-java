package com.carddemo.online.controller;

import com.carddemo.online.config.SecurityConfig;
import com.carddemo.online.dto.ReportRequest;
import com.carddemo.online.dto.ReportResponse;
import com.carddemo.online.security.JwtAuthenticationFilter;
import com.carddemo.online.security.JwtUtil;
import com.carddemo.online.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class, BatchAutoConfiguration.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void generateReport_validRequest_returns202() throws Exception {
        ReportRequest request = new ReportRequest("2024-01-01", "2024-01-31");
        ReportResponse response = new ReportResponse(1L, "STARTING", "/tmp/report.txt");
        when(reportService.generateReport(any(ReportRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reports/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.executionId").value(1))
                .andExpect(jsonPath("$.status").value("STARTING"))
                .andExpect(jsonPath("$.outputPath").value("/tmp/report.txt"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void generateReport_invalidDates_returns400() throws Exception {
        ReportRequest request = new ReportRequest("bad-date", "2024-01-31");

        mockMvc.perform(post("/api/reports/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void generateReport_missingDates_returns400() throws Exception {
        ReportRequest request = new ReportRequest();

        mockMvc.perform(post("/api/reports/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateReport_unauthenticated_returns401() throws Exception {
        ReportRequest request = new ReportRequest("2024-01-01", "2024-01-31");

        mockMvc.perform(post("/api/reports/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getReport_existingId_returnsOk() throws Exception {
        ReportResponse response = new ReportResponse(1L, "COMPLETED", "/tmp/report.txt");
        when(reportService.getReport(1L)).thenReturn(response);

        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getReport_notFound_returns404() throws Exception {
        when(reportService.getReport(999L))
                .thenThrow(new ReportService.ReportNotFoundException("Report execution not found: 999"));

        mockMvc.perform(get("/api/reports/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Report execution not found: 999"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void generateReport_jobFails_returns500() throws Exception {
        ReportRequest request = new ReportRequest("2024-01-01", "2024-01-31");
        when(reportService.generateReport(any(ReportRequest.class)))
                .thenThrow(new ReportService.ReportGenerationException("Job failed", new RuntimeException()));

        mockMvc.perform(post("/api/reports/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Job failed"));
    }
}
