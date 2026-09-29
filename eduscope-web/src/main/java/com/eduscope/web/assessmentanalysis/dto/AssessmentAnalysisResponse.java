package com.eduscope.web.assessmentanalysis.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 평가 분석 화면 응답 DTO.
 */
public class AssessmentAnalysisResponse {

    private final CourseInfo course;
    private final Long jobId;
    private final List<AssessmentItem> assessments;

    public AssessmentAnalysisResponse(
            CourseInfo course,
            Long jobId,
            List<AssessmentItem> assessments) {

        this.course = course;
        this.jobId = jobId;
        this.assessments = assessments;
    }

    public CourseInfo getCourse() {
        return course;
    }

    public Long getJobId() {
        return jobId;
    }

    public List<AssessmentItem> getAssessments() {
        return assessments;
    }


    public static class CourseInfo {

        private final Long coursePresentationId;
        private final Long datasetId;
        private final String codeModule;
        private final String codePresentation;

        public CourseInfo(
                Long coursePresentationId,
                Long datasetId,
                String codeModule,
                String codePresentation) {

            this.coursePresentationId =
                coursePresentationId;

            this.datasetId =
                datasetId;

            this.codeModule =
                codeModule;

            this.codePresentation =
                codePresentation;
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
    }


    public static class AssessmentItem {

        private final Long assessmentId;
        private final Long sourceAssessmentId;

        private final String assessmentType;

        private final Integer assessmentDueDay;

        private final BigDecimal assessmentWeight;

        private final Long submissionCount;

        private final BigDecimal avgScore;

        private final Long failCount;

        private final BigDecimal failRate;

        private final Long bankedCount;

        private final BigDecimal avgSubmissionDay;

        public AssessmentItem(
                Long assessmentId,
                Long sourceAssessmentId,
                String assessmentType,
                Integer assessmentDueDay,
                BigDecimal assessmentWeight,
                Long submissionCount,
                BigDecimal avgScore,
                Long failCount,
                BigDecimal failRate,
                Long bankedCount,
                BigDecimal avgSubmissionDay) {

            this.assessmentId =
                assessmentId;

            this.sourceAssessmentId =
                sourceAssessmentId;

            this.assessmentType =
                assessmentType;

            this.assessmentDueDay =
                assessmentDueDay;

            this.assessmentWeight =
                assessmentWeight;

            this.submissionCount =
                submissionCount;

            this.avgScore =
                avgScore;

            this.failCount =
                failCount;

            this.failRate =
                failRate;

            this.bankedCount =
                bankedCount;

            this.avgSubmissionDay =
                avgSubmissionDay;
        }

        public Long getAssessmentId() {
            return assessmentId;
        }

        public Long getSourceAssessmentId() {
            return sourceAssessmentId;
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
}