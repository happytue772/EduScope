package com.eduscope.loader.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 강의별 등록/수강취소 통계.
 */
@Entity
@Table(name = "REGISTRATION_STAT")
@IdClass(RegistrationStatId.class)
public class RegistrationStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(
        name = "COURSE_PRESENTATION_ID",
        nullable = false
    )
    private Long coursePresentationId;

    @Column(
        name = "REGISTRATION_COUNT",
        nullable = false
    )
    private Long registrationCount;

    @Column(
        name = "UNREGISTRATION_COUNT",
        nullable = false
    )
    private Long unregistrationCount;

    @Column(
        name = "WITHDRAWN_RESULT_COUNT",
        nullable = false
    )
    private Long withdrawnResultCount;

    @Column(
        name = "UNREGISTRATION_RATE",
        nullable = false,
        precision = 7,
        scale = 4
    )
    private BigDecimal unregistrationRate;

    @Column(
        name = "AVG_REGISTRATION_DAY",
        precision = 10,
        scale = 4
    )
    private BigDecimal avgRegistrationDay;

    @Column(
        name = "AVG_UNREGISTRATION_DAY",
        precision = 10,
        scale = 4
    )
    private BigDecimal avgUnregistrationDay;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public RegistrationStat() {
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public void setCoursePresentationId(
            Long coursePresentationId) {
        this.coursePresentationId = coursePresentationId;
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

    public void setWithdrawnResultCount(Long withdrawnResultCount) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}