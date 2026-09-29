package com.eduscope.web.dataset.dto;

import com.eduscope.web.dataset.entity.DatasetFile;

/**
 * DATASET_FILE 응답 DTO.
 */
public class DatasetFileResponse {

    private final Long datasetFileId;
    private final Long datasetId;

    private final String fileType;
    private final String originalFileName;

    private final Long fileSizeBytes;
    private final Long recordCount;

    private final String contentHash;
    private final String hdfsRawPath;
    private final String hdfsCleanPath;

    private final String loadStatus;

    public DatasetFileResponse(
            Long datasetFileId,
            Long datasetId,
            String fileType,
            String originalFileName,
            Long fileSizeBytes,
            Long recordCount,
            String contentHash,
            String hdfsRawPath,
            String hdfsCleanPath,
            String loadStatus) {

        this.datasetFileId = datasetFileId;
        this.datasetId = datasetId;
        this.fileType = fileType;
        this.originalFileName = originalFileName;
        this.fileSizeBytes = fileSizeBytes;
        this.recordCount = recordCount;
        this.contentHash = contentHash;
        this.hdfsRawPath = hdfsRawPath;
        this.hdfsCleanPath = hdfsCleanPath;
        this.loadStatus = loadStatus;
    }

    public static DatasetFileResponse from(
            DatasetFile file) {

        return new DatasetFileResponse(
            file.getDatasetFileId(),
            file.getDataset().getDatasetId(),
            file.getFileType(),
            file.getOriginalFileName(),
            file.getFileSizeBytes(),
            file.getRecordCount(),
            file.getContentHash(),
            file.getHdfsRawPath(),
            file.getHdfsCleanPath(),
            file.getLoadStatus()
        );
    }

    public Long getDatasetFileId() {
        return datasetFileId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public String getFileType() {
        return fileType;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public String getContentHash() {
        return contentHash;
    }

    public String getHdfsRawPath() {
        return hdfsRawPath;
    }

    public String getHdfsCleanPath() {
        return hdfsCleanPath;
    }

    public String getLoadStatus() {
        return loadStatus;
    }
}