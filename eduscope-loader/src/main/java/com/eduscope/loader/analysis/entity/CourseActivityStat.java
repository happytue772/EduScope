package com.eduscope.loader.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 강의별 전체 학습활동 집계 결과.
 */
@Entity
@Table(name = "COURSE_ACTIVITY_STAT")
@IdClass(CourseActivityStatId.class)
public class CourseActivityStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(
        name = "COURSE_PRESENTATION_ID",
        nullable = false
    )
    private Long coursePresentationId;

    @Column(
        name = "ACTIVE_STUDENT_COUNT",
        nullable = false
    )
    private Long activeStudentCount;

    @Column(
        name = "TOTAL_CLICK_COUNT",
        nullable = false
    )
    private Long totalClickCount;

    @Column(
        name = "AVG_CLICK_PER_STUDENT",
        nullable = false,
        precision = 19,
        scale = 4
    )
    private BigDecimal avgClickPerStudent;

    @Column(
        name = "ACTIVE_DAY_COUNT",
        nullable = false
    )
    private Long activeDayCount;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public CourseActivityStat() {
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

    public Long getActiveStudentCount() {
        return activeStudentCount;
    }

    public void setActiveStudentCount(
            Long activeStudentCount) {
        this.activeStudentCount = activeStudentCount;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public void setTotalClickCount(
            Long totalClickCount) {
        this.totalClickCount = totalClickCount;
    }

    public BigDecimal getAvgClickPerStudent() {
        return avgClickPerStudent;
    }

    public void setAvgClickPerStudent(
            BigDecimal avgClickPerStudent) {
        this.avgClickPerStudent = avgClickPerStudent;
    }

    public Long getActiveDayCount() {
        return activeDayCount;
    }

    public void setActiveDayCount(
            Long activeDayCount) {
        this.activeDayCount = activeDayCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}