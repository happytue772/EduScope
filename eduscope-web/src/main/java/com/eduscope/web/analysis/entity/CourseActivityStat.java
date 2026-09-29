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
 * 강의별 학습활동 통계 Entity.
 */
@Entity
@Table(name = "COURSE_ACTIVITY_STAT")
@IdClass(CourseActivityStatId.class)
public class CourseActivityStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(name = "COURSE_PRESENTATION_ID", nullable = false)
    private Long coursePresentationId;

    /**
     * 어떤 분석 Job에서 생성된 통계인지 조회.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "JOB_ID",
        insertable = false,
        updatable = false
    )
    private AnalysisJob analysisJob;

    /**
     * 어느 강의의 통계인지 조회.
     */
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

    @Column(name = "ACTIVE_DAY_COUNT", nullable = false)
    private Long activeDayCount;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected CourseActivityStat() {
        // JPA 기본 생성자
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
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

    public Long getActiveDayCount() {
        return activeDayCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}