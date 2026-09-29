package com.eduscope.web.analysis.dto;

import java.math.BigDecimal;

import com.eduscope.web.analysis.entity.ActivityResultStat;

/**
 * 학습결과별 활동 통계 React 응답 DTO.
 */
public class ActivityResultStatResponse {

    private final Long jobId;
    private final Long coursePresentationId;

    private final String codeModule;
    private final String codePresentation;

    private final String finalResult;

    private final Long studentCount;
    private final Long totalClickCount;

    private final BigDecimal avgClickCount;
    private final BigDecimal avgActiveDayCount;

    public ActivityResultStatResponse(
            Long jobId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            String finalResult,
            Long studentCount,
            Long totalClickCount,
            BigDecimal avgClickCount,
            BigDecimal avgActiveDayCount) {

        this.jobId = jobId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.finalResult = finalResult;
        this.studentCount = studentCount;
        this.totalClickCount = totalClickCount;
        this.avgClickCount = avgClickCount;
        this.avgActiveDayCount = avgActiveDayCount;
    }

    public static ActivityResultStatResponse from(
            ActivityResultStat stat) {

        return new ActivityResultStatResponse(
            stat.getJobId(),
            stat.getCoursePresentationId(),

            stat.getCoursePresentation().getCodeModule(),
            stat.getCoursePresentation().getCodePresentation(),

            stat.getFinalResult(),
            stat.getStudentCount(),
            stat.getTotalClickCount(),
            stat.getAvgClickCount(),
            stat.getAvgActiveDayCount()
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