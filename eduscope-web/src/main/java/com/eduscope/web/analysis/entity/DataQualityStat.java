package com.eduscope.web.analysis.entity;

import java.time.LocalDateTime;

import com.eduscope.web.dataset.entity.DatasetFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 데이터 품질 분석 통계 Entity.
 */
@Entity
@Table(name = "DATA_QUALITY_STAT")
@IdClass(DataQualityStatId.class)
public class DataQualityStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(name = "DATASET_FILE_ID", nullable = false)
    private Long datasetFileId;

    @Id
    @Column(name = "QUALITY_TYPE", nullable = false, length = 50)
    private String qualityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "JOB_ID",
        insertable = false,
        updatable = false
    )
    private AnalysisJob analysisJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "DATASET_FILE_ID",
        insertable = false,
        updatable = false
    )
    private DatasetFile datasetFile;

    @Column(name = "RECORD_COUNT", nullable = false)
    private Long recordCount;

    @Column(name = "SAMPLE_MESSAGE", length = 1000)
    private String sampleMessage;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected DataQualityStat() {
        // JPA 기본 생성자
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getDatasetFileId() {
        return datasetFileId;
    }

    public String getQualityType() {
        return qualityType;
    }

    public DatasetFile getDatasetFile() {
        return datasetFile;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public String getSampleMessage() {
        return sampleMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}