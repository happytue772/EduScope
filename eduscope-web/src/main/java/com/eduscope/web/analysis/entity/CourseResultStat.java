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
 * 강의별 최종 학습결과 통계.
 */
@Entity
@Table(name = "COURSE_RESULT_STAT")
@IdClass(CourseResultStatId.class)
public class CourseResultStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(name = "COURSE_PRESENTATION_ID", nullable = false)
    private Long coursePresentationId;

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

    @Column(name = "PASS_COUNT", nullable = false)
    private Long passCount;

    @Column(name = "FAIL_COUNT", nullable = false)
    private Long failCount;

    @Column(name = "WITHDRAWN_COUNT", nullable = false)
    private Long withdrawnCount;

    @Column(name = "DISTINCTION_COUNT", nullable = false)
    private Long distinctionCount;

    @Column(
        name = "PASS_RATE",
        nullable = false,
        precision = 7,
        scale = 4
    )
    private BigDecimal passRate;

    @Column(
        name = "FAIL_RATE",
        nullable = false,
        precision = 7,
        scale = 4
    )
    private BigDecimal failRate;

    @Column(
        name = "WITHDRAWN_RATE",
        nullable = false,
        precision = 7,
        scale = 4
    )
    private BigDecimal withdrawnRate;

    @Column(
        name = "DISTINCTION_RATE",
        nullable = false,
        precision = 7,
        scale = 4
    )
    private BigDecimal distinctionRate;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected CourseResultStat() {
        // JPA 기본 생성자
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public CoursePresentation getCoursePresentation() {
        return coursePresentation;
    }

    public Long getStudentCount() {
        return studentCount;
    }

    public Long getPassCount() {
        return passCount;
    }

    public Long getFailCount() {
        return failCount;
    }

    public Long getWithdrawnCount() {
        return withdrawnCount;
    }

    public Long getDistinctionCount() {
        return distinctionCount;
    }

    public BigDecimal getPassRate() {
        return passRate;
    }

    public BigDecimal getFailRate() {
        return failRate;
    }

    public BigDecimal getWithdrawnRate() {
        return withdrawnRate;
    }

    public BigDecimal getDistinctionRate() {
        return distinctionRate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}