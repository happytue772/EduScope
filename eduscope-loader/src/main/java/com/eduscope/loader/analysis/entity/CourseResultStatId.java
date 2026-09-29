package com.eduscope.loader.analysis.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * COURSE_RESULT_STAT 복합 PK.
 */
public class CourseResultStatId implements Serializable {

    private Long jobId;
    private Long coursePresentationId;

    public CourseResultStatId() {
    }

    public CourseResultStatId(
            Long jobId,
            Long coursePresentationId) {
        this.jobId = jobId;
        this.coursePresentationId = coursePresentationId;
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof CourseResultStatId that)) {
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
        return Objects.hash(jobId, coursePresentationId);
    }
}