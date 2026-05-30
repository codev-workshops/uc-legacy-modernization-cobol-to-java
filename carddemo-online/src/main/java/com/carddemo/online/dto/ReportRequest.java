package com.carddemo.online.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ReportRequest {

    @NotBlank(message = "startDate is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "startDate must be YYYY-MM-DD")
    private String startDate;

    @NotBlank(message = "endDate is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "endDate must be YYYY-MM-DD")
    private String endDate;

    public ReportRequest() {
    }

    public ReportRequest(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
