package com.eduscope.web.resultanalysis.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 최종성과별 학습활동 비교 응답 DTO.
 */
public class ResultAnalysisResponse {

    private final CourseInfo course;
    private final Long activityResultJobId;
    private final Long courseResultJobId;

    private final CourseResultSummary courseResult;
    private final List<ResultActivityItem> resultActivities;

    public ResultAnalysisResponse(
            CourseInfo course,
            Long activityResultJobId,
            Long courseResultJobId,
            CourseResultSummary courseResult,
            List<ResultActivityItem> resultActivities) {

        this.course = course;
        this.activityResultJobId = activityResultJobId;
        this.courseResultJobId = courseResultJobId;
        this.courseResult = courseResult;
        this.resultActivities = resultActivities;
    }

    public CourseInfo getCourse() {
        return course;
    }

    public Long getActivityResultJobId() {
        return activityResultJobId;
    }

    public Long getCourseResultJobId() {
        return courseResultJobId;
    }

    public CourseResultSummary getCourseResult() {
        return courseResult;
    }

    public List<ResultActivityItem> getResultActivities() {
        return resultActivities;
    }

    /**
     * 선택 강의 정보.
     */
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

            this.coursePresentationId = coursePresentationId;
            this.datasetId = datasetId;
            this.codeModule = codeModule;
            this.codePresentation = codePresentation;
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

    /**
     * COURSE_RESULT_STAT 요약.
     */
    public static class CourseResultSummary {

        private final Long studentCount;

        private final Long passCount;
        private final Long failCount;
        private final Long withdrawnCount;
        private final Long distinctionCount;

        private final BigDecimal passRate;
        private final BigDecimal failRate;
        private final BigDecimal withdrawnRate;
        private final BigDecimal distinctionRate;

        public CourseResultSummary(
                Long studentCount,
                Long passCount,
                Long failCount,
                Long withdrawnCount,
                Long distinctionCount,
                BigDecimal passRate,
                BigDecimal failRate,
                BigDecimal withdrawnRate,
                BigDecimal distinctionRate) {

            this.studentCount = studentCount;
            this.passCount = passCount;
            this.failCount = failCount;
            this.withdrawnCount = withdrawnCount;
            this.distinctionCount = distinctionCount;
            this.passRate = passRate;
            this.failRate = failRate;
            this.withdrawnRate = withdrawnRate;
            this.distinctionRate = distinctionRate;
        }

        public Long getStudentCount() {
            return studentCount;
        }

        public Long getPassCount() {
            return passCount;
        }

        public Long getFailCount() {
            return failCount;
        }

        public Long getWithdrawnCount() {
            return withdrawnCount;
        }

        public Long getDistinctionCount() {
            return distinctionCount;
        }

        public BigDecimal getPassRate() {
            return passRate;
        }

        public BigDecimal getFailRate() {
            return failRate;
        }

        public BigDecimal getWithdrawnRate() {
            return withdrawnRate;
        }

        public BigDecimal getDistinctionRate() {
            return distinctionRate;
        }
    }

    /**
     * ACTIVITY_RESULT_STAT의
     * 최종 결과 그룹별 학습활동.
     */
    public static class ResultActivityItem {

        private final String finalResult;

        private final Long studentCount;
        private final Long totalClickCount;

        private final BigDecimal avgClickCount;
        private final BigDecimal avgActiveDayCount;

        public ResultActivityItem(
                String finalResult,
                Long studentCount,
                Long totalClickCount,
                BigDecimal avgClickCount,
                BigDecimal avgActiveDayCount) {

            this.finalResult = finalResult;
            this.studentCount = studentCount;
            this.totalClickCount = totalClickCount;
            this.avgClickCount = avgClickCount;
            this.avgActiveDayCount = avgActiveDayCount;
        }

        public String getFinalResult() {
            return finalResult;
        }

        public Long getStudentCount() {
            return studentCount;
        }

        public Long getTotalClickCount() {
            return totalClickCount;
        }

        public BigDecimal getAvgClickCount() {
            return avgClickCount;
        }

        public BigDecimal getAvgActiveDayCount() {
            return avgActiveDayCount;
        }
    }
}