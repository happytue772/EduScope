package com.eduscope.web.analysis.dto;

import java.math.BigDecimal;

import com.eduscope.web.analysis.entity.CourseResultStat;

/**
 * 강의 결과 통계 React 응답 DTO.
 */
public class CourseResultStatResponse {

    private final Long jobId;
    private final Long coursePresentationId;

    private final String codeModule;
    private final String codePresentation;

    private final Long studentCount;

    private final Long passCount;
    private final Long failCount;
    private final Long withdrawnCount;
    private final Long distinctionCount;

    private final BigDecimal passRate;
    private final BigDecimal failRate;
    private final BigDecimal withdrawnRate;
    private final BigDecimal distinctionRate;

    public CourseResultStatResponse(
            Long jobId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
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
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
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

    public static CourseResultStatResponse from(
            CourseResultStat stat) {

        return new CourseResultStatResponse(
            stat.getJobId(),
            stat.getCoursePresentationId(),

            stat.getCoursePresentation().getCodeModule(),
            stat.getCoursePresentation().getCodePresentation(),

            stat.getStudentCount(),
            stat.getPassCount(),
            stat.getFailCount(),
            stat.getWithdrawnCount(),
            stat.getDistinctionCount(),

            stat.getPassRate(),
            stat.getFailRate(),
            stat.getWithdrawnRate(),
            stat.getDistinctionRate()
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