package com.eduscope.loader.analysis.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * COURSE_WEEKLY_ACTIVITY_STAT 복합 PK.
 * PK = JOB_ID + COURSE_PRESENTATION_ID + RELATIVE_WEEK_NO
 */
public class CourseWeeklyActivityStatId implements Serializable {

    private Long jobId;
    private Long coursePresentationId;
    private Integer relativeWeekNo;

    public CourseWeeklyActivityStatId() {
    }

    public CourseWeeklyActivityStatId(
            Long jobId,
            Long coursePresentationId,
            Integer relativeWeekNo) {

        this.jobId = jobId;
        this.coursePresentationId = coursePresentationId;
        this.relativeWeekNo = relativeWeekNo;
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

    public Integer getRelativeWeekNo() {
        return relativeWeekNo;
    }

    public void setRelativeWeekNo(
            Integer relativeWeekNo) {
        this.relativeWeekNo = relativeWeekNo;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof CourseWeeklyActivityStatId that)) {
            return false;
        }

        return Objects.equals(jobId, that.jobId)
                && Objects.equals(
                    coursePresentationId,
                    that.coursePresentationId
                )
                && Objects.equals(
                    relativeWeekNo,
                    that.relativeWeekNo
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            jobId,
            coursePresentationId,
            relativeWeekNo
        );
    }
}