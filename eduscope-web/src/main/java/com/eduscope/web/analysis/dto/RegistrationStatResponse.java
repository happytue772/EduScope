package com.eduscope.web.analysis.dto;

import java.math.BigDecimal;

import com.eduscope.web.analysis.entity.RegistrationStat;

/**
 * 등록/수강취소 통계 React 응답 DTO.
 */
public class RegistrationStatResponse {

    private final Long jobId;

    private final Long coursePresentationId;
    private final String codeModule;
    private final String codePresentation;

    private final Long registrationCount;
    private final Long unregistrationCount;
    private final Long withdrawnResultCount;

    private final BigDecimal unregistrationRate;
    private final BigDecimal avgRegistrationDay;
    private final BigDecimal avgUnregistrationDay;

    public RegistrationStatResponse(
            Long jobId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            Long registrationCount,
            Long unregistrationCount,
            Long withdrawnResultCount,
            BigDecimal unregistrationRate,
            BigDecimal avgRegistrationDay,
            BigDecimal avgUnregistrationDay) {

        this.jobId = jobId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.registrationCount = registrationCount;
        this.unregistrationCount = unregistrationCount;
        this.withdrawnResultCount = withdrawnResultCount;
        this.unregistrationRate = unregistrationRate;
        this.avgRegistrationDay = avgRegistrationDay;
        this.avgUnregistrationDay = avgUnregistrationDay;
    }

    /**
     * RegistrationStat Entity를
     * REST Response DTO로 변환한다.
     */
    public static RegistrationStatResponse from(
            RegistrationStat stat) {

        return new RegistrationStatResponse(
            stat.getJobId(),
            stat.getCoursePresentationId(),

            stat.getCoursePresentation()
                .getCodeModule(),

            stat.getCoursePresentation()
                .getCodePresentation(),

            stat.getRegistrationCount(),
            stat.getUnregistrationCount(),
            stat.getWithdrawnResultCount(),
            stat.getUnregistrationRate(),
            stat.getAvgRegistrationDay(),
            stat.getAvgUnregistrationDay()
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