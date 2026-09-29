package com.eduscope.loader.analysis.dto;

import java.math.BigDecimal;

/**
 * course-activity.tsv 한 행 DTO.
 */
public class CourseActivityStatCsvRow {

    private String codeModule;
    private String codePresentation;

    private Long activeStudentCount;
    private Long totalClickCount;
    private BigDecimal avgClickPerStudent;
    private Long activeDayCount;

    public CourseActivityStatCsvRow() {
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

    public Long getActiveStudentCount() {
        return activeStudentCount;
    }

    public void setActiveStudentCount(
            Long activeStudentCount) {
        this.activeStudentCount = activeStudentCount;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public void setTotalClickCount(
            Long totalClickCount) {
        this.totalClickCount = totalClickCount;
    }

    public BigDecimal getAvgClickPerStudent() {
        return avgClickPerStudent;
    }

    public void setAvgClickPerStudent(
            BigDecimal avgClickPerStudent) {
        this.avgClickPerStudent = avgClickPerStudent;
    }

    public Long getActiveDayCount() {
        return activeDayCount;
    }

    public void setActiveDayCount(Long activeDayCount) {
        this.activeDayCount = activeDayCount;
    }
}