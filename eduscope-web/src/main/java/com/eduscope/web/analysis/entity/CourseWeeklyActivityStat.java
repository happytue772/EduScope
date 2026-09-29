package com.eduscope.web.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eduscope.web.course.entity.CoursePresentation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 강의별 주차 단위 활동통계.
 */
@Entity
@Table(name = "COURSE_WEEKLY_ACTIVITY_STAT")
@IdClass(CourseWeeklyActivityStatId.class)
public class CourseWeeklyActivityStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(name = "COURSE_PRESENTATION_ID", nullable = false)
    private Long coursePresentationId;

    @Id
    @Column(name = "RELATIVE_WEEK_NO", nullable = false)
    private Integer relativeWeekNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "JOB_ID",
        insertable = false,
        updatable = false
    )
    private AnalysisJob analysisJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "COURSE_PRESENTATION_ID",
        insertable = false,
        updatable = false
    )
    private CoursePresentation coursePresentation;

    @Column(name = "ACTIVE_STUDENT_COUNT", nullable = false)
    private Long activeStudentCount;

    @Column(name = "TOTAL_CLICK_COUNT", nullable = false)
    private Long totalClickCount;

    @Column(
        name = "AVG_CLICK_PER_STUDENT",
        nullable = false,
        precision = 19,
        scale = 4
    )
    private BigDecimal avgClickPerStudent;

    @Column(name = "ACTIVE_MATERIAL_COUNT", nullable = false)
    private Long activeMaterialCount;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected CourseWeeklyActivityStat() {
        // JPA 기본 생성자
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public Integer getRelativeWeekNo() {
        return relativeWeekNo;
    }

    public AnalysisJob getAnalysisJob() {
        return analysisJob;
    }

    public CoursePresentation getCoursePresentation() {
        return coursePresentation;
    }

    public Long getActiveStudentCount() {
        return activeStudentCount;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public BigDecimal getAvgClickPerStudent() {
        return avgClickPerStudent;
    }

    public Long getActiveMaterialCount() {
        return activeMaterialCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}