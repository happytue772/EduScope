package com.eduscope.web.analysis.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * ACTIVITY_RESULT_STAT 복합 PK.
 */
public class ActivityResultStatId implements Serializable {

    private Long jobId;
    private Long coursePresentationId;
    private String finalResult;

    public ActivityResultStatId() {
    }

    public ActivityResultStatId(
            Long jobId,
            Long coursePresentationId,
            String finalResult) {

        this.jobId = jobId;
        this.coursePresentationId = coursePresentationId;
        this.finalResult = finalResult;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public void setCoursePresentationId(
            Long coursePresentationId) {

        this.coursePresentationId = coursePresentationId;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(String finalResult) {
        this.finalResult = finalResult;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof ActivityResultStatId that)) {
            return false;
        }

        return Objects.equals(jobId, that.jobId)
            && Objects.equals(
                coursePresentationId,
                that.coursePresentationId
            )
            && Objects.equals(
                finalResult,
                that.finalResult
            );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            jobId,
            coursePresentationId,
            finalResult
        );
    }
}