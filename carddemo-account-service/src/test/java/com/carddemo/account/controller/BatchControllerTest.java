package com.carddemo.account.controller;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatchController.class)
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobLauncher jobLauncher;

    @MockBean(name = "jobs")
    private Map<String, Job> jobs;

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
    void launchJob_success() throws Exception {
        Job mockJob = mock(Job.class);
        when(mockJob.getName()).thenReturn("accountReaderJob");
        when(jobs.get("accountReaderJob")).thenReturn(mockJob);

        JobExecution execution = mock(JobExecution.class);
        when(execution.getId()).thenReturn(1L);
        when(execution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobLauncher.run(eq(mockJob), any())).thenReturn(execution);

        mockMvc.perform(post("/api/batch/jobs/accountReaderJob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobExecutionId").value(1))
                .andExpect(jsonPath("$.jobName").value("accountReaderJob"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void launchJob_notFound() throws Exception {
        when(jobs.get("nonExistentJob")).thenReturn(null);

        mockMvc.perform(post("/api/batch/jobs/nonExistentJob"))
                .andExpect(status().isNotFound());
    }

    @Test
    void launchJob_failure() throws Exception {
        Job mockJob = mock(Job.class);
        when(jobs.get("failingJob")).thenReturn(mockJob);
        when(jobLauncher.run(eq(mockJob), any())).thenThrow(new RuntimeException("Job failed"));

        mockMvc.perform(post("/api/batch/jobs/failingJob"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Job failed"));
    }
}
