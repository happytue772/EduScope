package com.eduscope.loader.analysis.dto;

import java.math.BigDecimal;

/**
 * course-weekly-activity.tsv 한 행 DTO.
 */
public class CourseWeeklyActivityStatCsvRow {

    private String codeModule;
    private String codePresentation;
    private Integer relativeWeekNo;

    private Long activeStudentCount;
    private Long totalClickCount;
    private BigDecimal avgClickPerStudent;
    private Long activeMaterialCount;

    public CourseWeeklyActivityStatCsvRow() {
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

    public Integer getRelativeWeekNo() {
        return relativeWeekNo;
    }

    public void setRelativeWeekNo(
            Integer relativeWeekNo) {
        this.relativeWeekNo = relativeWeekNo;
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

    public Long getActiveMaterialCount() {
        return activeMaterialCount;
    }

    public void setActiveMaterialCount(
            Long activeMaterialCount) {
        this.activeMaterialCount = activeMaterialCount;
    }
}