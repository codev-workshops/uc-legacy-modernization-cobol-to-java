package com.carddemo.transaction.controller;

import com.carddemo.transaction.exception.GlobalExceptionHandler;
import com.carddemo.transaction.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(GlobalExceptionHandler.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Test
    void generateTransactionReport() throws Exception {
        byte[] reportData = "DALYREPT Report Data".getBytes();
        when(reportService.generateTransactionReport(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))
                .thenReturn(reportData);

        mockMvc.perform(get("/api/reports/transactions")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=transaction_report.txt"))
                .andExpect(content().bytes(reportData));
    }

    @Test
    void generateTransactionReport_missingParams() throws Exception {
        mockMvc.perform(get("/api/reports/transactions"))
                .andExpect(status().isBadRequest());
    }
}
