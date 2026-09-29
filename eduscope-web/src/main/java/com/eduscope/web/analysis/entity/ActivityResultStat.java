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
 * 최종 학습결과별 활동 통계 Entity.
 */
@Entity
@Table(name = "ACTIVITY_RESULT_STAT")
@IdClass(ActivityResultStatId.class)
public class ActivityResultStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(name = "COURSE_PRESENTATION_ID", nullable = false)
    private Long coursePresentationId;

    @Id
    @Column(name = "FINAL_RESULT", nullable = false, length = 30)
    private String finalResult;

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

    @Column(name = "STUDENT_COUNT", nullable = false)
    private Long studentCount;

    @Column(name = "TOTAL_CLICK_COUNT", nullable = false)
    private Long totalClickCount;

    @Column(
        name = "AVG_CLICK_COUNT",
        nullable = false,
        precision = 19,
        scale = 4
    )
    private BigDecimal avgClickCount;

    @Column(
        name = "AVG_ACTIVE_DAY_COUNT",
        nullable = false,
        precision = 19,
        scale = 4
    )
    private BigDecimal avgActiveDayCount;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected ActivityResultStat() {
        // JPA 기본 생성자
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public CoursePresentation getCoursePresentation() {
        return coursePresentation;
    }

    public Long getStudentCount() {
        return studentCount;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public BigDecimal getAvgClickCount() {
        return avgClickCount;
    }

    public BigDecimal getAvgActiveDayCount() {
        return avgActiveDayCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}