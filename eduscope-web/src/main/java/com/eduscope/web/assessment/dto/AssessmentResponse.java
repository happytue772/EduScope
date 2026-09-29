package com.eduscope.web.assessment.dto;

import java.math.BigDecimal;

import com.eduscope.web.assessment.entity.Assessment;

/**
 * React에 전달할 평가 기준정보 응답 DTO.
 */
public class AssessmentResponse {

    private final Long assessmentId;
    private final Long sourceAssessmentId;

    private final Long datasetId;

    private final Long coursePresentationId;
    private final String codeModule;
    private final String codePresentation;

    private final String assessmentType;
    private final Integer assessmentDueDay;
    private final BigDecimal assessmentWeight;

    public AssessmentResponse(
            Long assessmentId,
            Long sourceAssessmentId,
            Long datasetId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            String assessmentType,
            Integer assessmentDueDay,
            BigDecimal assessmentWeight) {

        this.assessmentId = assessmentId;
        this.sourceAssessmentId = sourceAssessmentId;
        this.datasetId = datasetId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.assessmentType = assessmentType;
        this.assessmentDueDay = assessmentDueDay;
        this.assessmentWeight = assessmentWeight;
    }

    /**
     * Entity → REST Response DTO 변환.
     */
    public static AssessmentResponse from(
            Assessment assessment) {

        return new AssessmentResponse(
            assessment.getAssessmentId(),
            assessment.getSourceAssessmentId(),

            assessment.getDataset()
                      .getDatasetId(),

            assessment.getCoursePresentation()
                      .getCoursePresentationId(),

            assessment.getCoursePresentation()
                      .getCodeModule(),

            assessment.getCoursePresentation()
                      .getCodePresentation(),

            assessment.getAssessmentType(),
            assessment.getAssessmentDueDay(),
            assessment.getAssessmentWeight()
        );
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public Long getSourceAssessmentId() {
        return sourceAssessmentId;
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

    public String getAssessmentType() {
        return assessmentType;
    }

    public Integer getAssessmentDueDay() {
        return assessmentDueDay;
    }

    public BigDecimal getAssessmentWeight() {
        return assessmentWeight;
    }
}