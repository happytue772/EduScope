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
 * 강의별 등록/수강취소 분석 통계.
 */
@Entity
@Table(name = "REGISTRATION_STAT")
@IdClass(RegistrationStatId.class)
public class RegistrationStat {

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

    @Column(name = "REGISTRATION_COUNT", nullable = false)
    private Long registrationCount;

    @Column(name = "UNREGISTRATION_COUNT", nullable = false)
    private Long unregistrationCount;

    @Column(name = "WITHDRAWN_RESULT_COUNT", nullable = false)
    private Long withdrawnResultCount;

    @Column(
        name = "UNREGISTRATION_RATE",
        nullable = false,
        precision = 7,
        scale = 4
    )
    private BigDecimal unregistrationRate;

    /**
     * Loader 단계에서 실제 TSV 정밀도에 맞춰
     * NUMBER(10,4)로 유지한 값.
     */
    @Column(
        name = "AVG_REGISTRATION_DAY",
        precision = 10,
        scale = 4
    )
    private BigDecimal avgRegistrationDay;

    @Column(
        name = "AVG_UNREGISTRATION_DAY",
        precision = 10,
        scale = 4
    )
    private BigDecimal avgUnregistrationDay;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected RegistrationStat() {
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

    public Long getRegistrationCount() {
        return registrationCount;
    }

    public Long getUnregistrationCount() {
        return unregistrationCount;
    }

    public Long getWithdrawnResultCount() {
        return withdrawnResultCount;
    }

    public BigDecimal getUnregistrationRate() {
        return unregistrationRate;
    }

    public BigDecimal getAvgRegistrationDay() {
        return avgRegistrationDay;
    }

    public BigDecimal getAvgUnregistrationDay() {
        return avgUnregistrationDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}