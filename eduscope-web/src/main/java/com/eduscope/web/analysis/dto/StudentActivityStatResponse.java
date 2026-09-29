package com.eduscope.web.analysis.dto;

import java.math.BigDecimal;

import com.eduscope.web.analysis.entity.StudentActivityStat;

/**
 * 학생 활동 통계 REST 응답 DTO.
 */
public class StudentActivityStatResponse {

    private final Long jobId;
    private final Long studentCourseId;

    private final Long studentId;
    private final Long sourceStudentId;

    private final Long coursePresentationId;
    private final String codeModule;
    private final String codePresentation;

    private final Long totalClickCount;
    private final Long activeDayCount;
    private final Long usedMaterialCount;

    private final BigDecimal avgDailyClickCount;

    private final Integer firstActivityDay;
    private final Integer lastActivityDay;

    public StudentActivityStatResponse(
            Long jobId,
            Long studentCourseId,
            Long studentId,
            Long sourceStudentId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            Long totalClickCount,
            Long activeDayCount,
            Long usedMaterialCount,
            BigDecimal avgDailyClickCount,
            Integer firstActivityDay,
            Integer lastActivityDay) {

        this.jobId = jobId;
        this.studentCourseId = studentCourseId;
        this.studentId = studentId;
        this.sourceStudentId = sourceStudentId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.totalClickCount = totalClickCount;
        this.activeDayCount = activeDayCount;
        this.usedMaterialCount = usedMaterialCount;
        this.avgDailyClickCount = avgDailyClickCount;
        this.firstActivityDay = firstActivityDay;
        this.lastActivityDay = lastActivityDay;
    }

    public static StudentActivityStatResponse from(
            StudentActivityStat stat) {

        return new StudentActivityStatResponse(
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

            stat.getTotalClickCount(),
            stat.getActiveDayCount(),
            stat.getUsedMaterialCount(),
            stat.getAvgDailyClickCount(),
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