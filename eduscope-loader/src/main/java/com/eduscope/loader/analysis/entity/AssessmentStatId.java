package com.eduscope.loader.analysis.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * ASSESSMENT_STAT 복합 PK.
 * PK = JOB_ID + ASSESSMENT_ID
 */
public class AssessmentStatId implements Serializable {

    private Long jobId;
    private Long assessmentId;

    public AssessmentStatId() {
    }

    public AssessmentStatId(
            Long jobId,
            Long assessmentId) {

        this.jobId = jobId;
        this.assessmentId = assessmentId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof AssessmentStatId that)) {
            return false;
        }

        return Objects.equals(jobId, that.jobId)
            && Objects.equals(
                assessmentId,
                that.assessmentId
            );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            jobId,
            assessmentId
        );
    }
}