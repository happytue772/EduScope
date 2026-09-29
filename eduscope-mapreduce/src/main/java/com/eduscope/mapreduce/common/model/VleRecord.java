package com.eduscope.mapreduce.common.model;

/**
 * OULAD vle.csv 한 행을 표현하는 모델.
 */
public class VleRecord {

    private final long siteId;
    private final String codeModule;
    private final String codePresentation;
    private final String activityType;

    // 원본에서 빈 값이 가능하므로 Integer 사용
    private final Integer weekFrom;
    private final Integer weekTo;

    public VleRecord(
            long siteId,
            String codeModule,
            String codePresentation,
            String activityType,
            Integer weekFrom,
            Integer weekTo) {

        this.siteId = siteId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.activityType = activityType;
        this.weekFrom = weekFrom;
        this.weekTo = weekTo;
    }

    public long getSiteId() {
        return siteId;
    }

    public String getCodeModule() {
        return codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public String getActivityType() {
        return activityType;
    }

    public Integer getWeekFrom() {
        return weekFrom;
    }

    public Integer getWeekTo() {
        return weekTo;
    }
}