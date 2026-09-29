package com.eduscope.loader.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 과제/시험별 제출 및 성적 분석 통계.
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

    @Column(
        name = "SUBMISSION_COUNT",
        nullable = false
    )
    private Long submissionCount;

    @Column(
        name = "AVG_SCORE",
        nullable = false,
        precision = 7,
        scale = 2
    )
    private BigDecimal avgScore;

    @Column(
        name = "FAIL_COUNT",
        nullable = false
    )
    private Long failCount;

    @Column(
        name = "FAIL_RATE",
        nullable = false,
        precision = 7,
        scale = 4
    )
    private BigDecimal failRate;

    @Column(
        name = "BANKED_COUNT",
        nullable = false
    )
    private Long bankedCount;

    @Column(
        name = "AVG_SUBMISSION_DAY",
        precision = 10,
        scale = 4
    )
    private BigDecimal avgSubmissionDay;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public AssessmentStat() {
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Long getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(Long submissionCount) {
        this.submissionCount = submissionCount;
    }

    public BigDecimal getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(BigDecimal avgScore) {
        this.avgScore = avgScore;
    }

    public Long getFailCount() {
        return failCount;
    }

    public void setFailCount(Long failCount) {
        this.failCount = failCount;
    }

    public BigDecimal getFailRate() {
        return failRate;
    }

    public void setFailRate(BigDecimal failRate) {
        this.failRate = failRate;
    }

    public Long getBankedCount() {
        return bankedCount;
    }

    public void setBankedCount(Long bankedCount) {
        this.bankedCount = bankedCount;
    }

    public BigDecimal getAvgSubmissionDay() {
        return avgSubmissionDay;
    }

    public void setAvgSubmissionDay(
            BigDecimal avgSubmissionDay) {
        this.avgSubmissionDay = avgSubmissionDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}