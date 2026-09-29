package com.eduscope.web.registrationanalysis.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * EduScope 수강/철회 분석 응답 DTO.
 */
public class RegistrationAnalysisResponse {

    private final CourseInfo course;
    private final Long jobId;
    private final RegistrationSummary summary;

    private final List<DailyCount> registrationByDay;
    private final List<DailyCount> unregistrationByDay;

    public RegistrationAnalysisResponse(
            CourseInfo course,
            Long jobId,
            RegistrationSummary summary,
            List<DailyCount> registrationByDay,
            List<DailyCount> unregistrationByDay) {

        this.course = course;
        this.jobId = jobId;
        this.summary = summary;
        this.registrationByDay = registrationByDay;
        this.unregistrationByDay = unregistrationByDay;
    }

    public CourseInfo getCourse() {
        return course;
    }

    public Long getJobId() {
        return jobId;
    }

    public RegistrationSummary getSummary() {
        return summary;
    }

    public List<DailyCount> getRegistrationByDay() {
        return registrationByDay;
    }

    public List<DailyCount> getUnregistrationByDay() {
        return unregistrationByDay;
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
     * REGISTRATION_STAT의 실제 분석 결과.
     */
    public static class RegistrationSummary {

        private final Long registrationCount;
        private final Long unregistrationCount;
        private final Long withdrawnResultCount;

        private final BigDecimal unregistrationRate;
        private final BigDecimal avgRegistrationDay;
        private final BigDecimal avgUnregistrationDay;

        public RegistrationSummary(
                Long registrationCount,
                Long unregistrationCount,
                Long withdrawnResultCount,
                BigDecimal unregistrationRate,
                BigDecimal avgRegistrationDay,
                BigDecimal avgUnregistrationDay) {

            this.registrationCount = registrationCount;
            this.unregistrationCount = unregistrationCount;
            this.withdrawnResultCount = withdrawnResultCount;
            this.unregistrationRate = unregistrationRate;
            this.avgRegistrationDay = avgRegistrationDay;
            this.avgUnregistrationDay = avgUnregistrationDay;
        }

        public Long getRegistrationCount() {
            return registrationCount;
        }

        public Long getUnregistrationCount() {
            return unregistrationCount;
        }

        public Long getWithdrawnResultCount() {
            return withdrawnResultCount;
        }

        public BigDecimal getUnregistrationRate() {
            return unregistrationRate;
        }

        public BigDecimal getAvgRegistrationDay() {
            return avgRegistrationDay;
        }

        public BigDecimal getAvgUnregistrationDay() {
            return avgUnregistrationDay;
        }
    }

    /**
     * 특정 상대일의 등록/철회 건수.
     */
    public static class DailyCount {

        private final Integer relativeDay;
        private final Long count;

        public DailyCount(
                Integer relativeDay,
                Long count) {

            this.relativeDay = relativeDay;
            this.count = count;
        }

        public Integer getRelativeDay() {
            return relativeDay;
        }

        public Long getCount() {
            return count;
        }
    }
}