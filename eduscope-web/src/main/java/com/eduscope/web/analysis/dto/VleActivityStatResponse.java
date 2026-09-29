package com.eduscope.web.analysis.dto;

import com.eduscope.web.analysis.entity.VleActivityStat;

/**
 * VLE 활동통계 React 응답 DTO.
 */
public class VleActivityStatResponse {

    private final Long jobId;

    private final Long vleMaterialId;
    private final Long sourceSiteId;

    private final Long coursePresentationId;
    private final String codeModule;
    private final String codePresentation;

    private final String activityType;

    private final Long totalClickCount;
    private final Long activeStudentCount;
    private final Long activeDayCount;

    public VleActivityStatResponse(
            Long jobId,
            Long vleMaterialId,
            Long sourceSiteId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            String activityType,
            Long totalClickCount,
            Long activeStudentCount,
            Long activeDayCount) {

        this.jobId = jobId;
        this.vleMaterialId = vleMaterialId;
        this.sourceSiteId = sourceSiteId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.activityType = activityType;
        this.totalClickCount = totalClickCount;
        this.activeStudentCount = activeStudentCount;
        this.activeDayCount = activeDayCount;
    }

    /**
     * Entity → REST DTO 변환.
     */
    public static VleActivityStatResponse from(
            VleActivityStat stat) {

        return new VleActivityStatResponse(
            stat.getJobId(),

            stat.getVleMaterialId(),

            stat.getVleMaterial()
                .getSourceSiteId(),

            stat.getVleMaterial()
                .getCoursePresentation()
                .getCoursePresentationId(),

            stat.getVleMaterial()
                .getCoursePresentation()
                .getCodeModule(),

            stat.getVleMaterial()
                .getCoursePresentation()
                .getCodePresentation(),

            stat.getVleMaterial()
                .getActivityType(),

            stat.getTotalClickCount(),
            stat.getActiveStudentCount(),
            stat.getActiveDayCount()
        );
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getVleMaterialId() {
        return vleMaterialId;
    }

    public Long getSourceSiteId() {
        return sourceSiteId;
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

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public Long getActiveStudentCount() {
        return activeStudentCount;
    }

    public Long getActiveDayCount() {
        return activeDayCount;
    }
}