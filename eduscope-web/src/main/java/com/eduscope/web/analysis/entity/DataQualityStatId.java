package com.eduscope.web.analysis.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * DATA_QUALITY_STAT 복합 PK.
 */
public class DataQualityStatId implements Serializable {

    private Long jobId;
    private Long datasetFileId;
    private String qualityType;

    public DataQualityStatId() {
    }

    public DataQualityStatId(
            Long jobId,
            Long datasetFileId,
            String qualityType) {

        this.jobId = jobId;
        this.datasetFileId = datasetFileId;
        this.qualityType = qualityType;
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

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof DataQualityStatId that)) {
            return false;
        }

        return Objects.equals(jobId, that.jobId)
            && Objects.equals(datasetFileId, that.datasetFileId)
            && Objects.equals(qualityType, that.qualityType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            jobId,
            datasetFileId,
            qualityType
        );
    }
}