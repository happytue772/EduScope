package com.eduscope.loader.analysis.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * COURSE_ACTIVITY_STAT 복합 PK.
 * PK = JOB_ID + COURSE_PRESENTATION_ID
 */
public class CourseActivityStatId implements Serializable {

    private Long jobId;
    private Long coursePresentationId;

    public CourseActivityStatId() {
    }

    public CourseActivityStatId(
            Long jobId,
            Long coursePresentationId) {

        this.jobId = jobId;
        this.coursePresentationId = coursePresentationId;
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

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof CourseActivityStatId that)) {
            return false;
        }

        return Objects.equals(jobId, that.jobId)
                && Objects.equals(
                    coursePresentationId,
                    that.coursePresentationId
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            jobId,
            coursePresentationId
        );
    }
}