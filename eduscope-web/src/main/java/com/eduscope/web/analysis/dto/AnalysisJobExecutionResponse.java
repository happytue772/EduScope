package com.eduscope.web.analysis.dto;

import java.time.LocalDateTime;

/**
 * 실제 Analysis Job 실행 요청 응답.
 */
public class AnalysisJobExecutionResponse {

    private final Long jobId;

    private final String status;

    private final LocalDateTime startedAt;


    public AnalysisJobExecutionResponse(
            Long jobId,
            String status,
            LocalDateTime startedAt) {

        this.jobId =
            jobId;

        this.status =
            status;

        this.startedAt =
            startedAt;
    }


    public Long getJobId() {
        return jobId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }
}