package com.carddemo.batch.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BatchJobControllerUnitTest {

    private final BatchJobController controller = new BatchJobController();

    @Test
    void startJob_returnsAcceptedWithJobName() {
        ResponseEntity<Map<String, Object>> response = controller.startJob("dailyProcess");
        assertEquals(202, response.getStatusCode().value());
        assertEquals("dailyProcess", response.getBody().get("jobName"));
        assertEquals("ACCEPTED", response.getBody().get("status"));
    }

    @Test
    void getJobStatus_returnsOkWithJobId() {
        ResponseEntity<Map<String, Object>> response = controller.getJobStatus(42L);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(42L, response.getBody().get("jobId"));
        assertEquals("UNKNOWN", response.getBody().get("status"));
    }
}
