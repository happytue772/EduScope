package com.eduscope.loader.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 강의 개설학기별 주차 단위 학습활동 집계.
 */
@Entity
@Table(name = "COURSE_WEEKLY_ACTIVITY_STAT")
@IdClass(CourseWeeklyActivityStatId.class)
public class CourseWeeklyActivityStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(
        name = "COURSE_PRESENTATION_ID",
        nullable = false
    )
    private Long coursePresentationId;

    @Id
    @Column(
        name = "RELATIVE_WEEK_NO",
        nullable = false
    )
    private Integer relativeWeekNo;

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
        name = "ACTIVE_MATERIAL_COUNT",
        nullable = false
    )
    private Long activeMaterialCount;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public CourseWeeklyActivityStat() {
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

    public Long getActiveMaterialCount() {
        return activeMaterialCount;
    }

    public void setActiveMaterialCount(
            Long activeMaterialCount) {
        this.activeMaterialCount = activeMaterialCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}