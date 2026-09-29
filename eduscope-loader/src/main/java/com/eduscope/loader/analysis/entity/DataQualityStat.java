package com.eduscope.loader.analysis.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 데이터 파일별 품질 분석 집계 결과.
 */
@Entity
@Table(name = "DATA_QUALITY_STAT")
@IdClass(DataQualityStatId.class)
public class DataQualityStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(
        name = "DATASET_FILE_ID",
        nullable = false
    )
    private Long datasetFileId;

    @Id
    @Column(
        name = "QUALITY_TYPE",
        nullable = false,
        length = 50
    )
    private String qualityType;

    @Column(
        name = "RECORD_COUNT",
        nullable = false
    )
    private Long recordCount;

    @Column(
        name = "SAMPLE_MESSAGE",
        length = 1000
    )
    private String sampleMessage;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public DataQualityStat() {
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getDatasetFileId() {
        return datasetFileId;
    }

    public void setDatasetFileId(Long datasetFileId) {
        this.datasetFileId = datasetFileId;
    }

    public String getQualityType() {
        return qualityType;
    }

    public void setQualityType(String qualityType) {
        this.qualityType = qualityType;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }

    public String getSampleMessage() {
        return sampleMessage;
    }

    public void setSampleMessage(String sampleMessage) {
        this.sampleMessage = sampleMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}