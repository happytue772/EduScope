package com.eduscope.loader.reference.dto;

/**
 * vle.csv 한 행 DTO.
 */
public class VleMaterialCsvRow {

    private Long sourceSiteId;
    private String codeModule;
    private String codePresentation;
    private String activityType;
    private Integer weekFrom;
    private Integer weekTo;

    public VleMaterialCsvRow() {
    }

    public Long getSourceSiteId() {
        return sourceSiteId;
    }

    public void setSourceSiteId(Long sourceSiteId) {
        this.sourceSiteId = sourceSiteId;
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

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Integer getWeekFrom() {
        return weekFrom;
    }

    public void setWeekFrom(Integer weekFrom) {
        this.weekFrom = weekFrom;
    }

    public Integer getWeekTo() {
        return weekTo;
    }

    public void setWeekTo(Integer weekTo) {
        this.weekTo = weekTo;
    }
}