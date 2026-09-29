package com.eduscope.loader.analysis.dto;

import java.math.BigDecimal;

/**
 * student-learning-summary.tsv 한 행 DTO.
 */
public class StudentLearningSummaryStatCsvRow {

    private String codeModule;
    private String codePresentation;
    private Long sourceStudentId;

    private Long totalClickCount;
    private Long activeDayCount;
    private Long usedMaterialCount;

    private Long submittedAssessmentCount;
    private BigDecimal avgAssessmentScore;
    private Long failedAssessmentCount;

    private Integer firstActivityDay;
    private Integer lastActivityDay;

    public StudentLearningSummaryStatCsvRow() {
    }

    public String getCodeModule() {
        return codeModule;
    }

    public void setCodeModule(String codeModule) {
        this.codeModule = codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public void setCodePresentation(String codePresentation) {
        this.codePresentation = codePresentation;
    }

    public Long getSourceStudentId() {
        return sourceStudentId;
    }

    public void setSourceStudentId(Long sourceStudentId) {
        this.sourceStudentId = sourceStudentId;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public void setTotalClickCount(Long totalClickCount) {
        this.totalClickCount = totalClickCount;
    }

    public Long getActiveDayCount() {
        return activeDayCount;
    }

    public void setActiveDayCount(Long activeDayCount) {
        this.activeDayCount = activeDayCount;
    }

    public Long getUsedMaterialCount() {
        return usedMaterialCount;
    }

    public void setUsedMaterialCount(Long usedMaterialCount) {
        this.usedMaterialCount = usedMaterialCount;
    }

    public Long getSubmittedAssessmentCount() {
        return submittedAssessmentCount;
    }

    public void setSubmittedAssessmentCount(
            Long submittedAssessmentCount) {
        this.submittedAssessmentCount = submittedAssessmentCount;
    }

    public BigDecimal getAvgAssessmentScore() {
        return avgAssessmentScore;
    }

    public void setAvgAssessmentScore(
            BigDecimal avgAssessmentScore) {
        this.avgAssessmentScore = avgAssessmentScore;
    }

    public Long getFailedAssessmentCount() {
        return failedAssessmentCount;
    }

    public void setFailedAssessmentCount(
            Long failedAssessmentCount) {
        this.failedAssessmentCount = failedAssessmentCount;
    }

    public Integer getFirstActivityDay() {
        return firstActivityDay;
    }

    public void setFirstActivityDay(Integer firstActivityDay) {
        this.firstActivityDay = firstActivityDay;
    }

    public Integer getLastActivityDay() {
        return lastActivityDay;
    }

    public void setLastActivityDay(Integer lastActivityDay) {
        this.lastActivityDay = lastActivityDay;
    }
}