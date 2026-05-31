package com.carddemo.batch.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BatchJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void startJob_returnsAccepted() throws Exception {
        mockMvc.perform(post("/api/v1/batch/jobs/testJob/start"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.jobName").value("testJob"))
            .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void getJobStatus_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/batch/jobs/1/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobId").value(1))
            .andExpect(jsonPath("$.status").value("UNKNOWN"));
    }
}
