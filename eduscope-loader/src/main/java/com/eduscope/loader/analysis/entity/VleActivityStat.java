package com.eduscope.loader.analysis.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * VLE 자료별 학습활동 집계 결과.
 */
@Entity
@Table(name = "VLE_ACTIVITY_STAT")
@IdClass(VleActivityStatId.class)
public class VleActivityStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(
        name = "VLE_MATERIAL_ID",
        nullable = false
    )
    private Long vleMaterialId;

    @Column(
        name = "TOTAL_CLICK_COUNT",
        nullable = false
    )
    private Long totalClickCount;

    @Column(
        name = "ACTIVE_STUDENT_COUNT",
        nullable = false
    )
    private Long activeStudentCount;

    @Column(
        name = "ACTIVE_DAY_COUNT",
        nullable = false
    )
    private Long activeDayCount;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public VleActivityStat() {
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getVleMaterialId() {
        return vleMaterialId;
    }

    public void setVleMaterialId(Long vleMaterialId) {
        this.vleMaterialId = vleMaterialId;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public void setTotalClickCount(Long totalClickCount) {
        this.totalClickCount = totalClickCount;
    }

    public Long getActiveStudentCount() {
        return activeStudentCount;
    }

    public void setActiveStudentCount(Long activeStudentCount) {
        this.activeStudentCount = activeStudentCount;
    }

    public Long getActiveDayCount() {
        return activeDayCount;
    }

    public void setActiveDayCount(Long activeDayCount) {
        this.activeDayCount = activeDayCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}