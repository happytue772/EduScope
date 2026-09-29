package com.eduscope.web.analysis.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * STUDENT_LEARNING_SUMMARY_STAT 복합 PK.
 */
public class StudentLearningSummaryStatId implements Serializable {

    private Long jobId;
    private Long studentCourseId;

    public StudentLearningSummaryStatId() {
    }

    public StudentLearningSummaryStatId(
            Long jobId,
            Long studentCourseId) {

        this.jobId = jobId;
        this.studentCourseId = studentCourseId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getStudentCourseId() {
        return studentCourseId;
    }

    public void setStudentCourseId(Long studentCourseId) {
        this.studentCourseId = studentCourseId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof StudentLearningSummaryStatId that)) {
            return false;
        }

        return Objects.equals(jobId, that.jobId)
            && Objects.equals(
                studentCourseId,
                that.studentCourseId
            );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            jobId,
            studentCourseId
        );
    }
}