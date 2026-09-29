package com.eduscope.loader.analysis.dto;

import java.math.BigDecimal;

/**
 * registration.tsv 한 행 DTO.
 */
public class RegistrationStatCsvRow {

    private String codeModule;
    private String codePresentation;

    private Long registrationCount;
    private Long unregistrationCount;
    private Long withdrawnResultCount;

    private BigDecimal unregistrationRate;
    private BigDecimal avgRegistrationDay;
    private BigDecimal avgUnregistrationDay;

    public RegistrationStatCsvRow() {
    }

    public String getCodeModule() {
        return codeModule;
    }

    public void setCodeModule(String codeModule) {
        this.codeModule = codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public void setCodePresentation(String codePresentation) {
        this.codePresentation = codePresentation;
    }

    public Long getRegistrationCount() {
        return registrationCount;
    }

    public void setRegistrationCount(Long registrationCount) {
        this.registrationCount = registrationCount;
    }

    public Long getUnregistrationCount() {
        return unregistrationCount;
    }

    public void setUnregistrationCount(Long unregistrationCount) {
        this.unregistrationCount = unregistrationCount;
    }

    public Long getWithdrawnResultCount() {
        return withdrawnResultCount;
    }

    public void setWithdrawnResultCount(
            Long withdrawnResultCount) {
        this.withdrawnResultCount = withdrawnResultCount;
    }

    public BigDecimal getUnregistrationRate() {
        return unregistrationRate;
    }

    public void setUnregistrationRate(
            BigDecimal unregistrationRate) {
        this.unregistrationRate = unregistrationRate;
    }

    public BigDecimal getAvgRegistrationDay() {
        return avgRegistrationDay;
    }

    public void setAvgRegistrationDay(
            BigDecimal avgRegistrationDay) {
        this.avgRegistrationDay = avgRegistrationDay;
    }

    public BigDecimal getAvgUnregistrationDay() {
        return avgUnregistrationDay;
    }

    public void setAvgUnregistrationDay(
            BigDecimal avgUnregistrationDay) {
        this.avgUnregistrationDay = avgUnregistrationDay;
    }
}