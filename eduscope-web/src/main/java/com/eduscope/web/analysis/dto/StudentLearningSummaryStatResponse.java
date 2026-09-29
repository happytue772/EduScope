package com.eduscope.web.analysis.dto;

import java.math.BigDecimal;

import com.eduscope.web.analysis.entity.StudentLearningSummaryStat;

/**
 * 학생 학습 종합 통계 React 응답 DTO.
 */
public class StudentLearningSummaryStatResponse {

    private final Long jobId;
    private final Long studentCourseId;

    private final Long studentId;
    private final Long sourceStudentId;

    private final Long coursePresentationId;
    private final String codeModule;
    private final String codePresentation;

    private final String finalResult;

    private final Long totalClickCount;
    private final Long activeDayCount;
    private final Long usedMaterialCount;

    private final Long submittedAssessmentCount;
    private final BigDecimal avgAssessmentScore;
    private final Long failedAssessmentCount;

    private final Integer firstActivityDay;
    private final Integer lastActivityDay;

    public StudentLearningSummaryStatResponse(
            Long jobId,
            Long studentCourseId,
            Long studentId,
            Long sourceStudentId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            String finalResult,
            Long totalClickCount,
            Long activeDayCount,
            Long usedMaterialCount,
            Long submittedAssessmentCount,
            BigDecimal avgAssessmentScore,
            Long failedAssessmentCount,
            Integer firstActivityDay,
            Integer lastActivityDay) {

        this.jobId = jobId;
        this.studentCourseId = studentCourseId;
        this.studentId = studentId;
        this.sourceStudentId = sourceStudentId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.finalResult = finalResult;
        this.totalClickCount = totalClickCount;
        this.activeDayCount = activeDayCount;
        this.usedMaterialCount = usedMaterialCount;
        this.submittedAssessmentCount = submittedAssessmentCount;
        this.avgAssessmentScore = avgAssessmentScore;
        this.failedAssessmentCount = failedAssessmentCount;
        this.firstActivityDay = firstActivityDay;
        this.lastActivityDay = lastActivityDay;
    }

    public static StudentLearningSummaryStatResponse from(
            StudentLearningSummaryStat stat) {

        return new StudentLearningSummaryStatResponse(
            stat.getJobId(),
            stat.getStudentCourseId(),

            stat.getStudentCourse()
                .getStudent()
                .getStudentId(),

            stat.getStudentCourse()
                .getStudent()
                .getSourceStudentId(),

            stat.getStudentCourse()
                .getCoursePresentation()
                .getCoursePresentationId(),

            stat.getStudentCourse()
                .getCoursePresentation()
                .getCodeModule(),

            stat.getStudentCourse()
                .getCoursePresentation()
                .getCodePresentation(),

            stat.getStudentCourse()
                .getFinalResult(),

            stat.getTotalClickCount(),
            stat.getActiveDayCount(),
            stat.getUsedMaterialCount(),

            stat.getSubmittedAssessmentCount(),
            stat.getAvgAssessmentScore(),
            stat.getFailedAssessmentCount(),

            stat.getFirstActivityDay(),
            stat.getLastActivityDay()
        );
    }

    public Long getJobId() {
        return jobId;
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

    public String getCodeModule() {
        return codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public String getFinalResult() {
        return finalResult;
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