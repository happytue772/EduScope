package com.eduscope.web.analysis.dto;

import java.math.BigDecimal;

import com.eduscope.web.analysis.entity.CourseActivityStat;

/**
 * 강의 활동통계 React 응답 DTO.
 */
public class CourseActivityStatResponse {

    private final Long jobId;
    private final Long coursePresentationId;

    private final String codeModule;
    private final String codePresentation;

    private final Long activeStudentCount;
    private final Long totalClickCount;
    private final BigDecimal avgClickPerStudent;
    private final Long activeDayCount;

    public CourseActivityStatResponse(
            Long jobId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            Long activeStudentCount,
            Long totalClickCount,
            BigDecimal avgClickPerStudent,
            Long activeDayCount) {

        this.jobId = jobId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.activeStudentCount = activeStudentCount;
        this.totalClickCount = totalClickCount;
        this.avgClickPerStudent = avgClickPerStudent;
        this.activeDayCount = activeDayCount;
    }

    public static CourseActivityStatResponse from(
            CourseActivityStat stat) {

        return new CourseActivityStatResponse(
            stat.getJobId(),
            stat.getCoursePresentationId(),

            stat.getCoursePresentation().getCodeModule(),
            stat.getCoursePresentation().getCodePresentation(),

            stat.getActiveStudentCount(),
            stat.getTotalClickCount(),
            stat.getAvgClickPerStudent(),
            stat.getActiveDayCount()
        );
    }

    public Long getJobId() {
        return jobId;
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