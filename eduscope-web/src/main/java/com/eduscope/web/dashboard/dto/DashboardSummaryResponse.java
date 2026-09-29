package com.eduscope.web.dashboard.dto;

/**
 * Dashboard 핵심 KPI 응답 DTO.
 *
 * 기존 Oracle 데이터와
 * 최신 성공 분석 결과를 조합해서 반환한다.
 */
public class DashboardSummaryResponse {

    private final long datasetCount;
    private final long datasetFileCount;

    private final long coursePresentationCount;

    // OULAD 익명 학생 수
    private final long uniqueStudentCount;

    // 학생-강의 수강 건수
    private final long enrollmentCount;

    private final long totalClickCount;

    private final long passCount;
    private final long failCount;
    private final long withdrawnCount;
    private final long distinctionCount;

    private final Long courseActivityJobId;
    private final Long courseResultJobId;

    public DashboardSummaryResponse(
            long datasetCount,
            long datasetFileCount,
            long coursePresentationCount,
            long uniqueStudentCount,
            long enrollmentCount,
            long totalClickCount,
            long passCount,
            long failCount,
            long withdrawnCount,
            long distinctionCount,
            Long courseActivityJobId,
            Long courseResultJobId) {

        this.datasetCount = datasetCount;
        this.datasetFileCount = datasetFileCount;
        this.coursePresentationCount = coursePresentationCount;
        this.uniqueStudentCount = uniqueStudentCount;
        this.enrollmentCount = enrollmentCount;
        this.totalClickCount = totalClickCount;
        this.passCount = passCount;
        this.failCount = failCount;
        this.withdrawnCount = withdrawnCount;
        this.distinctionCount = distinctionCount;
        this.courseActivityJobId = courseActivityJobId;
        this.courseResultJobId = courseResultJobId;
    }

    public long getDatasetCount() {
        return datasetCount;
    }

    public long getDatasetFileCount() {
        return datasetFileCount;
    }

    public long getCoursePresentationCount() {
        return coursePresentationCount;
    }

    public long getUniqueStudentCount() {
        return uniqueStudentCount;
    }

    public long getEnrollmentCount() {
        return enrollmentCount;
    }

    public long getTotalClickCount() {
        return totalClickCount;
    }

    public long getPassCount() {
        return passCount;
    }

    public long getFailCount() {
        return failCount;
    }

    public long getWithdrawnCount() {
        return withdrawnCount;
    }

    public long getDistinctionCount() {
        return distinctionCount;
    }

    public Long getCourseActivityJobId() {
        return courseActivityJobId;
    }

    public Long getCourseResultJobId() {
        return courseResultJobId;
    }
}