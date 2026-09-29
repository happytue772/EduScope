package com.eduscope.loader.reference.dto;

import java.math.BigDecimal;

/**
 * studentAssessment.csv 한 행 DTO.
 */
public class StudentAssessmentCsvRow {

    private Long sourceAssessmentId;
    private Long sourceStudentId;
    private Integer submittedDay;
    private Integer bankedValue;
    private BigDecimal score;

    public StudentAssessmentCsvRow() {
    }

    public Long getSourceAssessmentId() {
        return sourceAssessmentId;
    }

    public void setSourceAssessmentId(Long sourceAssessmentId) {
        this.sourceAssessmentId = sourceAssessmentId;
    }

    public Long getSourceStudentId() {
        return sourceStudentId;
    }

    public void setSourceStudentId(Long sourceStudentId) {
        this.sourceStudentId = sourceStudentId;
    }

    public Integer getSubmittedDay() {
        return submittedDay;
    }

    public void setSubmittedDay(Integer submittedDay) {
        this.submittedDay = submittedDay;
    }

    public Integer getBankedValue() {
        return bankedValue;
    }

    public void setBankedValue(Integer bankedValue) {
        this.bankedValue = bankedValue;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
}