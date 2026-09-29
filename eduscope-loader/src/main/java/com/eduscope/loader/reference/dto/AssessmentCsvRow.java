package com.eduscope.loader.reference.dto;

import java.math.BigDecimal;

/**
 * assessments.csv 한 행 DTO.
 */
public class AssessmentCsvRow {

    private String codeModule;
    private String codePresentation;
    private Long sourceAssessmentId;
    private String assessmentType;
    private Integer assessmentDueDay;
    private BigDecimal assessmentWeight;

    public AssessmentCsvRow() {
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

    public void setCodePresentation(
            String codePresentation) {
        this.codePresentation = codePresentation;
    }

    public Long getSourceAssessmentId() {
        return sourceAssessmentId;
    }

    public void setSourceAssessmentId(
            Long sourceAssessmentId) {
        this.sourceAssessmentId = sourceAssessmentId;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(
            String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public Integer getAssessmentDueDay() {
        return assessmentDueDay;
    }

    public void setAssessmentDueDay(
            Integer assessmentDueDay) {
        this.assessmentDueDay = assessmentDueDay;
    }

    public BigDecimal getAssessmentWeight() {
        return assessmentWeight;
    }

    public void setAssessmentWeight(
            BigDecimal assessmentWeight) {
        this.assessmentWeight = assessmentWeight;
    }
}