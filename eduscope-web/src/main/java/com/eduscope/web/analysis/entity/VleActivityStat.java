package com.eduscope.web.analysis.entity;

import java.time.LocalDateTime;

import com.eduscope.web.vle.entity.VleMaterial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * VLE 학습자료별 활동 통계 Entity.
 */
@Entity
@Table(name = "VLE_ACTIVITY_STAT")
@IdClass(VleActivityStatId.class)
public class VleActivityStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(name = "VLE_MATERIAL_ID", nullable = false)
    private Long vleMaterialId;

    /**
     * 해당 통계를 생성한 분석 Job.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "JOB_ID",
        insertable = false,
        updatable = false
    )
    private AnalysisJob analysisJob;

    /**
     * 실제 VLE 학습자료와 연결.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "VLE_MATERIAL_ID",
        insertable = false,
        updatable = false
    )
    private VleMaterial vleMaterial;

    @Column(name = "TOTAL_CLICK_COUNT", nullable = false)
    private Long totalClickCount;

    @Column(name = "ACTIVE_STUDENT_COUNT", nullable = false)
    private Long activeStudentCount;

    @Column(name = "ACTIVE_DAY_COUNT", nullable = false)
    private Long activeDayCount;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected VleActivityStat() {
        // JPA 기본 생성자
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getVleMaterialId() {
        return vleMaterialId;
    }

    public AnalysisJob getAnalysisJob() {
        return analysisJob;
    }

    public VleMaterial getVleMaterial() {
        return vleMaterial;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public Long getActiveStudentCount() {
        return activeStudentCount;
    }

    public Long getActiveDayCount() {
        return activeDayCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}