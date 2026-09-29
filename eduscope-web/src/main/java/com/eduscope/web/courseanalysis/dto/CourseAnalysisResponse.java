package com.eduscope.web.courseanalysis.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 강의 분석 화면 전체 응답 DTO.
 *
 * 여러 기존 통계 테이블의 실제 데이터를
 * 한 화면에서 사용하기 좋게 조합한다.
 */
public class CourseAnalysisResponse {

    private final CourseInfo course;
    private final ActivitySummary activity;
    private final ResultSummary result;
    private final RegistrationSummary registration;
    private final List<WeeklyActivity> weeklyActivity;

    public CourseAnalysisResponse(
            CourseInfo course,
            ActivitySummary activity,
            ResultSummary result,
            RegistrationSummary registration,
            List<WeeklyActivity> weeklyActivity) {

        this.course = course;
        this.activity = activity;
        this.result = result;
        this.registration = registration;
        this.weeklyActivity = weeklyActivity;
    }

    public CourseInfo getCourse() {
        return course;
    }

    public ActivitySummary getActivity() {
        return activity;
    }

    public ResultSummary getResult() {
        return result;
    }

    public RegistrationSummary getRegistration() {
        return registration;
    }

    public List<WeeklyActivity> getWeeklyActivity() {
        return weeklyActivity;
    }

    /**
     * COURSE_PRESENTATION 정보.
     */
    public static class CourseInfo {

        private final Long coursePresentationId;
        private final Long datasetId;
        private final String codeModule;
        private final String codePresentation;
        private final Integer modulePresentationLength;

        public CourseInfo(
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

    /**
     * COURSE_ACTIVITY_STAT 요약.
     */
    public static class ActivitySummary {

        private final Long jobId;
        private final Long activeStudentCount;
        private final Long totalClickCount;
        private final BigDecimal avgClickPerStudent;
        private final Long activeDayCount;

        public ActivitySummary(
                Long jobId,
                Long activeStudentCount,
                Long totalClickCount,
                BigDecimal avgClickPerStudent,
                Long activeDayCount) {

            this.jobId = jobId;
            this.activeStudentCount = activeStudentCount;
            this.totalClickCount = totalClickCount;
            this.avgClickPerStudent = avgClickPerStudent;
            this.activeDayCount = activeDayCount;
        }

        public Long getJobId() {
            return jobId;
        }

        public Long getActiveStudentCount() {
            return activeStudentCount;
        }

        public Long getTotalClickCount() {
            return totalClickCount;
        }

        public BigDecimal getAvgClickPerStudent() {
            return avgClickPerStudent;
        }

        public Long getActiveDayCount() {
            return activeDayCount;
        }
    }

    /**
     * COURSE_RESULT_STAT 요약.
     *
     * Rate 값은 DB의 0~1 원본 값을 그대로 반환한다.
     */
    public static class ResultSummary {

        private final Long jobId;
        private final Long studentCount;

        private final Long passCount;
        private final Long failCount;
        private final Long withdrawnCount;
        private final Long distinctionCount;

        private final BigDecimal passRate;
        private final BigDecimal failRate;
        private final BigDecimal withdrawnRate;
        private final BigDecimal distinctionRate;

        public ResultSummary(
                Long jobId,
                Long studentCount,
                Long passCount,
                Long failCount,
                Long withdrawnCount,
                Long distinctionCount,
                BigDecimal passRate,
                BigDecimal failRate,
                BigDecimal withdrawnRate,
                BigDecimal distinctionRate) {

            this.jobId = jobId;
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

        public Long getJobId() {
            return jobId;
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
     * REGISTRATION_STAT 요약.
     */
    public static class RegistrationSummary {

        private final Long jobId;

        private final Long registrationCount;
        private final Long unregistrationCount;
        private final Long withdrawnResultCount;

        private final BigDecimal unregistrationRate;
        private final BigDecimal avgRegistrationDay;
        private final BigDecimal avgUnregistrationDay;

        public RegistrationSummary(
                Long jobId,
                Long registrationCount,
                Long unregistrationCount,
                Long withdrawnResultCount,
                BigDecimal unregistrationRate,
                BigDecimal avgRegistrationDay,
                BigDecimal avgUnregistrationDay) {

            this.jobId = jobId;
            this.registrationCount = registrationCount;
            this.unregistrationCount = unregistrationCount;
            this.withdrawnResultCount = withdrawnResultCount;
            this.unregistrationRate = unregistrationRate;
            this.avgRegistrationDay = avgRegistrationDay;
            this.avgUnregistrationDay = avgUnregistrationDay;
        }

        public Long getJobId() {
            return jobId;
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
     * COURSE_WEEKLY_ACTIVITY_STAT 한 주차 데이터.
     */
    public static class WeeklyActivity {

        private final Integer relativeWeekNo;

        private final Long activeStudentCount;
        private final Long totalClickCount;
        private final BigDecimal avgClickPerStudent;
        private final Long activeMaterialCount;

        public WeeklyActivity(
                Integer relativeWeekNo,
                Long activeStudentCount,
                Long totalClickCount,
                BigDecimal avgClickPerStudent,
                Long activeMaterialCount) {

            this.relativeWeekNo = relativeWeekNo;
            this.activeStudentCount = activeStudentCount;
            this.totalClickCount = totalClickCount;
            this.avgClickPerStudent = avgClickPerStudent;
            this.activeMaterialCount = activeMaterialCount;
        }

        public Integer getRelativeWeekNo() {
            return relativeWeekNo;
        }

        public Long getActiveStudentCount() {
            return activeStudentCount;
        }

        public Long getTotalClickCount() {
            return totalClickCount;
        }

        public BigDecimal getAvgClickPerStudent() {
            return avgClickPerStudent;
        }

        public Long getActiveMaterialCount() {
            return activeMaterialCount;
        }
    }
}