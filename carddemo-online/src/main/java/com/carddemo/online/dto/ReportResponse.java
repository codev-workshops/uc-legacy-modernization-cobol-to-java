package com.carddemo.online.dto;

public class ReportResponse {

    private Long executionId;
    private String status;
    private String outputPath;

    public ReportResponse() {
    }

    public ReportResponse(Long executionId, String status, String outputPath) {
        this.executionId = executionId;
        this.status = status;
        this.outputPath = outputPath;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
