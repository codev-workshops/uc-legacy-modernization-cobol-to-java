package com.carddemo.batch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/batch/jobs")
public class BatchJobController {

    @PostMapping("/{jobName}/start")
    public ResponseEntity<Map<String, Object>> startJob(@PathVariable String jobName) {
        return ResponseEntity.accepted().body(Map.of(
            "jobName", jobName,
            "status", "ACCEPTED",
            "message", "Job start request accepted (not yet implemented)"
        ));
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable Long jobId) {
        return ResponseEntity.ok(Map.of(
            "jobId", jobId,
            "status", "UNKNOWN",
            "message", "Job status lookup not yet implemented"
        ));
    }
}
