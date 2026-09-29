package com.eduscope.loader.analysis.dto;

import java.math.BigDecimal;

/**
 * assessment.tsv 한 행 DTO.
 */
public class AssessmentStatCsvRow {

    private Long sourceAssessmentId;

    private Long submissionCount;
    private BigDecimal avgScore;
    private Long failCount;
    private BigDecimal failRate;
    private Long bankedCount;
    private BigDecimal avgSubmissionDay;

    public AssessmentStatCsvRow() {
    }

    public Long getSourceAssessmentId() {
        return sourceAssessmentId;
    }

    public void setSourceAssessmentId(
            Long sourceAssessmentId) {
        this.sourceAssessmentId = sourceAssessmentId;
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
}