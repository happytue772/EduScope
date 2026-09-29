package com.eduscope.web.course.dto;

import com.eduscope.web.course.entity.CoursePresentation;

/**
 * React에 전달할 강의 응답 DTO.
 */
public class CoursePresentationResponse {

    private final Long coursePresentationId;
    private final Long datasetId;

    private final String codeModule;
    private final String codePresentation;

    private final Integer modulePresentationLength;

    public CoursePresentationResponse(
            Long coursePresentationId,
            Long datasetId,
            String codeModule,
            String codePresentation,
            Integer modulePresentationLength) {

        this.coursePresentationId = coursePresentationId;
        this.datasetId = datasetId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.modulePresentationLength = modulePresentationLength;
    }

    /**
     * Entity → REST Response DTO 변환.
     */
    public static CoursePresentationResponse from(
            CoursePresentation course) {

        return new CoursePresentationResponse(
            course.getCoursePresentationId(),
            course.getDataset().getDatasetId(),
            course.getCodeModule(),
            course.getCodePresentation(),
            course.getModulePresentationLength()
        );
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public String getCodeModule() {
        return codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public Integer getModulePresentationLength() {
        return modulePresentationLength;
    }
}