package com.eduscope.web.dataset.dto;

import com.eduscope.web.dataset.entity.Dataset;

/**
 * React REST API에 반환할 DATASET 응답 DTO.
 */
public class DatasetResponse {

    private final Long datasetId;
    private final String displayName;
    private final String sourceName;
    private final String datasetVersion;
    private final String schemaVersion;
    private final String hdfsBasePath;
    private final Long fileCount;

    public DatasetResponse(
            Long datasetId,
            String displayName,
            String sourceName,
            String datasetVersion,
            String schemaVersion,
            String hdfsBasePath,
            Long fileCount) {

        this.datasetId = datasetId;
        this.displayName = displayName;
        this.sourceName = sourceName;
        this.datasetVersion = datasetVersion;
        this.schemaVersion = schemaVersion;
        this.hdfsBasePath = hdfsBasePath;
        this.fileCount = fileCount;
    }

    /**
     * Entity를 REST 응답 DTO로 변환.
     */
    public static DatasetResponse from(Dataset dataset) {

        return new DatasetResponse(
            dataset.getDatasetId(),
            dataset.getDisplayName(),
            dataset.getSourceName(),
            dataset.getDatasetVersion(),
            dataset.getSchemaVersion(),
            dataset.getHdfsBasePath(),
            dataset.getFileCount()
        );
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getDatasetVersion() {
        return datasetVersion;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getHdfsBasePath() {
        return hdfsBasePath;
    }

    public Long getFileCount() {
        return fileCount;
    }
}