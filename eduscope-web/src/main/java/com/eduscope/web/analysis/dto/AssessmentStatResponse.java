package com.eduscope.web.analysis.dto;

import java.math.BigDecimal;

import com.eduscope.web.analysis.entity.AssessmentStat;

/**
 * 평가 통계 React 응답 DTO.
 */
public class AssessmentStatResponse {

    private final Long jobId;

    private final Long assessmentId;
    private final Long sourceAssessmentId;

    private final Long coursePresentationId;
    private final String codeModule;
    private final String codePresentation;

    private final String assessmentType;

    private final Long submissionCount;
    private final BigDecimal avgScore;

    private final Long failCount;
    private final BigDecimal failRate;

    private final Long bankedCount;
    private final BigDecimal avgSubmissionDay;

    public AssessmentStatResponse(
            Long jobId,
            Long assessmentId,
            Long sourceAssessmentId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            String assessmentType,
            Long submissionCount,
            BigDecimal avgScore,
            Long failCount,
            BigDecimal failRate,
            Long bankedCount,
            BigDecimal avgSubmissionDay) {

        this.jobId = jobId;
        this.assessmentId = assessmentId;
        this.sourceAssessmentId = sourceAssessmentId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.assessmentType = assessmentType;
        this.submissionCount = submissionCount;
        this.avgScore = avgScore;
        this.failCount = failCount;
        this.failRate = failRate;
        this.bankedCount = bankedCount;
        this.avgSubmissionDay = avgSubmissionDay;
    }

    /**
     * Entity → REST DTO 변환.
     */
    public static AssessmentStatResponse from(
            AssessmentStat stat) {

        return new AssessmentStatResponse(
            stat.getJobId(),

            stat.getAssessmentId(),

            stat.getAssessment()
                .getSourceAssessmentId(),

            stat.getAssessment()
                .getCoursePresentation()
                .getCoursePresentationId(),

            stat.getAssessment()
                .getCoursePresentation()
                .getCodeModule(),

            stat.getAssessment()
                .getCoursePresentation()
                .getCodePresentation(),

            stat.getAssessment()
                .getAssessmentType(),

            stat.getSubmissionCount(),
            stat.getAvgScore(),
            stat.getFailCount(),
            stat.getFailRate(),
            stat.getBankedCount(),
            stat.getAvgSubmissionDay()
        );
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public Long getSourceAssessmentId() {
        return sourceAssessmentId;
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

    public Long getSubmissionCount() {
        return submissionCount;
    }

    public BigDecimal getAvgScore() {
        return avgScore;
    }

    public Long getFailCount() {
        return failCount;
    }

    public BigDecimal getFailRate() {
        return failRate;
    }

    public Long getBankedCount() {
        return bankedCount;
    }

    public BigDecimal getAvgSubmissionDay() {
        return avgSubmissionDay;
    }
}