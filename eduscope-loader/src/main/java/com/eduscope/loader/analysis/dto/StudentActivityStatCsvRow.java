package com.eduscope.loader.analysis.dto;

import java.math.BigDecimal;

/**
 * student-activity.tsv 한 행 DTO.
 */
public class StudentActivityStatCsvRow {

    private String codeModule;
    private String codePresentation;
    private Long sourceStudentId;

    private Long totalClickCount;
    private Long activeDayCount;
    private Long usedMaterialCount;

    private BigDecimal avgDailyClickCount;

    private Integer firstActivityDay;
    private Integer lastActivityDay;

    public StudentActivityStatCsvRow() {
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

    public BigDecimal getAvgDailyClickCount() {
        return avgDailyClickCount;
    }

    public void setAvgDailyClickCount(
            BigDecimal avgDailyClickCount) {
        this.avgDailyClickCount = avgDailyClickCount;
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