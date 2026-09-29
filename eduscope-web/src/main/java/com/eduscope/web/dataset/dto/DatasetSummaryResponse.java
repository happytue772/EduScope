package com.eduscope.web.dataset.dto;

/**
 * Dataset 목록 조회용 DTO.
 */
public class DatasetSummaryResponse {

    private final Long datasetId;
    private final String displayName;
    private final String sourceName;

    private final String datasetVersion;
    private final String schemaVersion;

    private final Long fileCount;

    public DatasetSummaryResponse(
            Long datasetId,
            String displayName,
            String sourceName,
            String datasetVersion,
            String schemaVersion,
            Long fileCount) {

        this.datasetId = datasetId;
        this.displayName = displayName;
        this.sourceName = sourceName;
        this.datasetVersion = datasetVersion;
        this.schemaVersion = schemaVersion;
        this.fileCount = fileCount;
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

    public Long getFileCount() {
        return fileCount;
    }
}