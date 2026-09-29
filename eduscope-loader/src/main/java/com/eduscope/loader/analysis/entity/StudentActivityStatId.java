package com.eduscope.loader.analysis.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * STUDENT_ACTIVITY_STAT 복합 PK.
 * PK = JOB_ID + STUDENT_COURSE_ID
 */
public class StudentActivityStatId implements Serializable {

    private Long jobId;
    private Long studentCourseId;

    public StudentActivityStatId() {
    }

    public StudentActivityStatId(
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

        if (!(o instanceof StudentActivityStatId that)) {
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