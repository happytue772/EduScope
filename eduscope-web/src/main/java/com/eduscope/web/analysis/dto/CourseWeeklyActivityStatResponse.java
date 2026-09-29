package com.eduscope.web.analysis.dto;

import java.math.BigDecimal;

import com.eduscope.web.analysis.entity.CourseWeeklyActivityStat;

/**
 * 강의별 주차 활동통계 응답 DTO.
 */
public class CourseWeeklyActivityStatResponse {

    private final Long jobId;
    private final Long coursePresentationId;

    private final String codeModule;
    private final String codePresentation;

    private final Integer relativeWeekNo;

    private final Long activeStudentCount;
    private final Long totalClickCount;
    private final BigDecimal avgClickPerStudent;
    private final Long activeMaterialCount;

    public CourseWeeklyActivityStatResponse(
            Long jobId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            Integer relativeWeekNo,
            Long activeStudentCount,
            Long totalClickCount,
            BigDecimal avgClickPerStudent,
            Long activeMaterialCount) {

        this.jobId = jobId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.relativeWeekNo = relativeWeekNo;
        this.activeStudentCount = activeStudentCount;
        this.totalClickCount = totalClickCount;
        this.avgClickPerStudent = avgClickPerStudent;
        this.activeMaterialCount = activeMaterialCount;
    }

    public static CourseWeeklyActivityStatResponse from(
            CourseWeeklyActivityStat stat) {

        return new CourseWeeklyActivityStatResponse(
            stat.getJobId(),
            stat.getCoursePresentationId(),

            stat.getCoursePresentation().getCodeModule(),
            stat.getCoursePresentation().getCodePresentation(),

            stat.getRelativeWeekNo(),

            stat.getActiveStudentCount(),
            stat.getTotalClickCount(),
            stat.getAvgClickPerStudent(),
            stat.getActiveMaterialCount()
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