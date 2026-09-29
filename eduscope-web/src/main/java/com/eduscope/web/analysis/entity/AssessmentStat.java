package com.eduscope.web.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eduscope.web.assessment.entity.Assessment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 과제/시험별 분석 통계 Entity.
 */
@Entity
@Table(name = "ASSESSMENT_STAT")
@IdClass(AssessmentStatId.class)
public class AssessmentStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(name = "ASSESSMENT_ID", nullable = false)
    private Long assessmentId;

    /**
     * 어떤 MapReduce 분석에서 나온 결과인지 연결.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "JOB_ID",
        insertable = false,
        updatable = false
    )
    private AnalysisJob analysisJob;

    /**
     * 실제 평가 기준정보와 연결.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ASSESSMENT_ID",
        insertable = false,
        updatable = false
    )
    private Assessment assessment;

    @Column(name = "SUBMISSION_COUNT", nullable = false)
    private Long submissionCount;

    @Column(
        name = "AVG_SCORE",
        nullable = false,
        precision = 7,
        scale = 2
    )
    private BigDecimal avgScore;

    @Column(name = "FAIL_COUNT", nullable = false)
    private Long failCount;

    @Column(
        name = "FAIL_RATE",
        nullable = false,
        precision = 7,
        scale = 4
    )
    private BigDecimal failRate;

    @Column(name = "BANKED_COUNT", nullable = false)
    private Long bankedCount;

    /**
     * Loader 단계에서 실제 TSV 정밀도에 맞춰
     * Oracle NUMBER(10,4)로 확장한 컬럼.
     */
    @Column(
        name = "AVG_SUBMISSION_DAY",
        precision = 10,
        scale = 4
    )
    private BigDecimal avgSubmissionDay;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected AssessmentStat() {
        // JPA 기본 생성자
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public AnalysisJob getAnalysisJob() {
        return analysisJob;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public Long getSubmissionCount() {
        return submissionCount;
    }

    public BigDecimal getAvgScore() {
        return avgScore;
    }

    public Long getFailCount() {
        return failCount;
    }

    public BigDecimal getFailRate() {
        return failRate;
    }

    public Long getBankedCount() {
        return bankedCount;
    }

    public BigDecimal getAvgSubmissionDay() {
        return avgSubmissionDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}