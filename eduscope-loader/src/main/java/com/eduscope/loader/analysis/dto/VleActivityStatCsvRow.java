package com.eduscope.loader.analysis.dto;

/**
 * vle-activity.tsv 한 행 DTO.
 */
public class VleActivityStatCsvRow {

    private String codeModule;
    private String codePresentation;
    private Long sourceSiteId;

    private Long totalClickCount;
    private Long activeStudentCount;
    private Long activeDayCount;

    public VleActivityStatCsvRow() {
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

    public Long getSourceSiteId() {
        return sourceSiteId;
    }

    public void setSourceSiteId(Long sourceSiteId) {
        this.sourceSiteId = sourceSiteId;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public void setTotalClickCount(Long totalClickCount) {
        this.totalClickCount = totalClickCount;
    }

    public Long getActiveStudentCount() {
        return activeStudentCount;
    }

    public void setActiveStudentCount(Long activeStudentCount) {
        this.activeStudentCount = activeStudentCount;
    }

    public Long getActiveDayCount() {
        return activeDayCount;
    }

    public void setActiveDayCount(Long activeDayCount) {
        this.activeDayCount = activeDayCount;
    }
}