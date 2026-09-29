package com.eduscope.web.studentanalysis.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 학생 상세 분석 응답 DTO.
 *
 * 학생 개인 식별은 OULAD 원본의 익명화된
 * SOURCE_STUDENT_ID를 사용한다.
 */
public class StudentAnalysisResponse {

    private final StudentCourseInfo student;
    private final RegistrationInfo registration;

    private final Long studentActivityJobId;
    private final Long learningSummaryJobId;

    private final ActivitySummary activity;
    private final LearningSummary learningSummary;

    private final List<AssessmentHistory> assessments;

    public StudentAnalysisResponse(
            StudentCourseInfo student,
            RegistrationInfo registration,
            Long studentActivityJobId,
            Long learningSummaryJobId,
            ActivitySummary activity,
            LearningSummary learningSummary,
            List<AssessmentHistory> assessments) {

        this.student = student;
        this.registration = registration;
        this.studentActivityJobId = studentActivityJobId;
        this.learningSummaryJobId = learningSummaryJobId;
        this.activity = activity;
        this.learningSummary = learningSummary;
        this.assessments = assessments;
    }

    public StudentCourseInfo getStudent() {
        return student;
    }

    public RegistrationInfo getRegistration() {
        return registration;
    }

    public Long getStudentActivityJobId() {
        return studentActivityJobId;
    }

    public Long getLearningSummaryJobId() {
        return learningSummaryJobId;
    }

    public ActivitySummary getActivity() {
        return activity;
    }

    public LearningSummary getLearningSummary() {
        return learningSummary;
    }

    public List<AssessmentHistory> getAssessments() {
        return assessments;
    }


    /**
     * 학생 + 수강 강의 기본정보.
     */
    public static class StudentCourseInfo {

        private final Long studentCourseId;
        private final Long studentId;
        private final Long sourceStudentId;

        private final Long coursePresentationId;
        private final Long datasetId;

        private final String codeModule;
        private final String codePresentation;

        private final String gender;
        private final String region;
        private final String highestEducation;
        private final String imdBand;
        private final String ageBand;

        private final Integer numOfPrevAttempts;
        private final Integer studiedCredits;

        private final String disability;
        private final String finalResult;

        public StudentCourseInfo(
                Long studentCourseId,
                Long studentId,
                Long sourceStudentId,
                Long coursePresentationId,
                Long datasetId,
                String codeModule,
                String codePresentation,
                String gender,
                String region,
                String highestEducation,
                String imdBand,
                String ageBand,
                Integer numOfPrevAttempts,
                Integer studiedCredits,
                String disability,
                String finalResult) {

            this.studentCourseId = studentCourseId;
            this.studentId = studentId;
            this.sourceStudentId = sourceStudentId;
            this.coursePresentationId = coursePresentationId;
            this.datasetId = datasetId;
            this.codeModule = codeModule;
            this.codePresentation = codePresentation;
            this.gender = gender;
            this.region = region;
            this.highestEducation = highestEducation;
            this.imdBand = imdBand;
            this.ageBand = ageBand;
            this.numOfPrevAttempts = numOfPrevAttempts;
            this.studiedCredits = studiedCredits;
            this.disability = disability;
            this.finalResult = finalResult;
        }

        public Long getStudentCourseId() {
            return studentCourseId;
        }

        public Long getStudentId() {
            return studentId;
        }

        public Long getSourceStudentId() {
            return sourceStudentId;
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

        public String getGender() {
            return gender;
        }

        public String getRegion() {
            return region;
        }

        public String getHighestEducation() {
            return highestEducation;
        }

        public String getImdBand() {
            return imdBand;
        }

        public String getAgeBand() {
            return ageBand;
        }

        public Integer getNumOfPrevAttempts() {
            return numOfPrevAttempts;
        }

        public Integer getStudiedCredits() {
            return studiedCredits;
        }

        public String getDisability() {
            return disability;
        }

        public String getFinalResult() {
            return finalResult;
        }
    }


    /**
     * 실제 등록 / 철회 상대일.
     */
    public static class RegistrationInfo {

        private final Integer registrationDay;
        private final Integer unregistrationDay;

        public RegistrationInfo(
                Integer registrationDay,
                Integer unregistrationDay) {

            this.registrationDay = registrationDay;
            this.unregistrationDay = unregistrationDay;
        }

        public Integer getRegistrationDay() {
            return registrationDay;
        }

        public Integer getUnregistrationDay() {
            return unregistrationDay;
        }
    }


    /**
     * STUDENT_ACTIVITY_STAT.
     */
    public static class ActivitySummary {

        private final Long totalClickCount;
        private final Long activeDayCount;
        private final Long usedMaterialCount;

        private final BigDecimal avgDailyClickCount;

        private final Integer firstActivityDay;
        private final Integer lastActivityDay;

        public ActivitySummary(
                Long totalClickCount,
                Long activeDayCount,
                Long usedMaterialCount,
                BigDecimal avgDailyClickCount,
                Integer firstActivityDay,
                Integer lastActivityDay) {

            this.totalClickCount = totalClickCount;
            this.activeDayCount = activeDayCount;
            this.usedMaterialCount = usedMaterialCount;
            this.avgDailyClickCount = avgDailyClickCount;
            this.firstActivityDay = firstActivityDay;
            this.lastActivityDay = lastActivityDay;
        }

        public Long getTotalClickCount() {
            return totalClickCount;
        }

        public Long getActiveDayCount() {
            return activeDayCount;
        }

        public Long getUsedMaterialCount() {
            return usedMaterialCount;
        }

        public BigDecimal getAvgDailyClickCount() {
            return avgDailyClickCount;
        }

        public Integer getFirstActivityDay() {
            return firstActivityDay;
        }

        public Integer getLastActivityDay() {
            return lastActivityDay;
        }
    }


    /**
     * STUDENT_LEARNING_SUMMARY_STAT.
     */
    public static class LearningSummary {

        private final Long totalClickCount;
        private final Long activeDayCount;
        private final Long usedMaterialCount;

        private final Long submittedAssessmentCount;
        private final BigDecimal avgAssessmentScore;
        private final Long failedAssessmentCount;

        private final Integer firstActivityDay;
        private final Integer lastActivityDay;

        public LearningSummary(
                Long totalClickCount,
                Long activeDayCount,
                Long usedMaterialCount,
                Long submittedAssessmentCount,
                BigDecimal avgAssessmentScore,
                Long failedAssessmentCount,
                Integer firstActivityDay,
                Integer lastActivityDay) {

            this.totalClickCount = totalClickCount;
            this.activeDayCount = activeDayCount;
            this.usedMaterialCount = usedMaterialCount;
            this.submittedAssessmentCount = submittedAssessmentCount;
            this.avgAssessmentScore = avgAssessmentScore;
            this.failedAssessmentCount = failedAssessmentCount;
            this.firstActivityDay = firstActivityDay;
            this.lastActivityDay = lastActivityDay;
        }

        public Long getTotalClickCount() {
            return totalClickCount;
        }

        public Long getActiveDayCount() {
            return activeDayCount;
        }

        public Long getUsedMaterialCount() {
            return usedMaterialCount;
        }

        public Long getSubmittedAssessmentCount() {
            return submittedAssessmentCount;
        }

        public BigDecimal getAvgAssessmentScore() {
            return avgAssessmentScore;
        }

        public Long getFailedAssessmentCount() {
            return failedAssessmentCount;
        }

        public Integer getFirstActivityDay() {
            return firstActivityDay;
        }

        public Integer getLastActivityDay() {
            return lastActivityDay;
        }
    }


    /**
     * 학생 평가 제출 이력.
     */
    public static class AssessmentHistory {

        private final Long assessmentId;
        private final Long sourceAssessmentId;

        private final String assessmentType;

        private final Integer assessmentDueDay;
        private final BigDecimal assessmentWeight;

        private final Integer submittedDay;
        private final String isBanked;
        private final BigDecimal score;

        public AssessmentHistory(
                Long assessmentId,
                Long sourceAssessmentId,
                String assessmentType,
                Integer assessmentDueDay,
                BigDecimal assessmentWeight,
                Integer submittedDay,
                String isBanked,
                BigDecimal score) {

            this.assessmentId = assessmentId;
            this.sourceAssessmentId = sourceAssessmentId;
            this.assessmentType = assessmentType;
            this.assessmentDueDay = assessmentDueDay;
            this.assessmentWeight = assessmentWeight;
            this.submittedDay = submittedDay;
            this.isBanked = isBanked;
            this.score = score;
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

        public Integer getSubmittedDay() {
            return submittedDay;
        }

        public String getIsBanked() {
            return isBanked;
        }

        public BigDecimal getScore() {
            return score;
        }
    }
}