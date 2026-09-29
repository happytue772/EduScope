package com.eduscope.web.vle.dto;

import com.eduscope.web.vle.entity.VleMaterial;

/**
 * React에 전달할 VLE 학습자료 응답 DTO.
 */
public class VleMaterialResponse {

    private final Long vleMaterialId;
    private final Long sourceSiteId;

    private final Long datasetId;

    private final Long coursePresentationId;
    private final String codeModule;
    private final String codePresentation;

    private final String activityType;

    private final Integer weekFrom;
    private final Integer weekTo;

    public VleMaterialResponse(
            Long vleMaterialId,
            Long sourceSiteId,
            Long datasetId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            String activityType,
            Integer weekFrom,
            Integer weekTo) {

        this.vleMaterialId = vleMaterialId;
        this.sourceSiteId = sourceSiteId;
        this.datasetId = datasetId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.activityType = activityType;
        this.weekFrom = weekFrom;
        this.weekTo = weekTo;
    }

    /**
     * Entity → REST Response DTO 변환.
     */
    public static VleMaterialResponse from(
            VleMaterial material) {

        return new VleMaterialResponse(
            material.getVleMaterialId(),
            material.getSourceSiteId(),

            material.getDataset()
                    .getDatasetId(),

            material.getCoursePresentation()
                    .getCoursePresentationId(),

            material.getCoursePresentation()
                    .getCodeModule(),

            material.getCoursePresentation()
                    .getCodePresentation(),

            material.getActivityType(),
            material.getWeekFrom(),
            material.getWeekTo()
        );
    }

    public Long getVleMaterialId() {
        return vleMaterialId;
    }

    public Long getSourceSiteId() {
        return sourceSiteId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
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