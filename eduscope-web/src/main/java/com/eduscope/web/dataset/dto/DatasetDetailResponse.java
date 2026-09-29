package com.eduscope.web.dataset.dto;

import java.util.List;

/**
 * Dataset 상세정보 + Dataset File 응답.
 */
public class DatasetDetailResponse {

    private final DatasetInfo dataset;
    private final DatasetSummary summary;
    private final List<DatasetFileInfo> files;

    public DatasetDetailResponse(
            DatasetInfo dataset,
            DatasetSummary summary,
            List<DatasetFileInfo> files) {

        this.dataset = dataset;
        this.summary = summary;
        this.files = files;
    }

    public DatasetInfo getDataset() {
        return dataset;
    }

    public DatasetSummary getSummary() {
        return summary;
    }

    public List<DatasetFileInfo> getFiles() {
        return files;
    }


    /**
     * DATASET 실제 컬럼.
     */
    public static class DatasetInfo {

        private final Long datasetId;

        private final String displayName;
        private final String description;

        private final String sourceType;
        private final String sourceName;
        private final String sourceUrl;

        private final String datasetVersion;
        private final String schemaVersion;

        private final String hdfsBasePath;

        private final Long fileCount;

        public DatasetInfo(
                Long datasetId,
                String displayName,
                String description,
                String sourceType,
                String sourceName,
                String sourceUrl,
                String datasetVersion,
                String schemaVersion,
                String hdfsBasePath,
                Long fileCount) {

            this.datasetId = datasetId;
            this.displayName = displayName;
            this.description = description;
            this.sourceType = sourceType;
            this.sourceName = sourceName;
            this.sourceUrl = sourceUrl;
            this.datasetVersion = datasetVersion;
            this.schemaVersion = schemaVersion;
            this.hdfsBasePath = hdfsBasePath;
            this.fileCount = fileCount;
        }

        public Long getDatasetId() {
            return datasetId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public String getSourceType() {
            return sourceType;
        }

        public String getSourceName() {
            return sourceName;
        }

        public String getSourceUrl() {
            return sourceUrl;
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


    /**
     * 화면 KPI.
     *
     * DATASET_FILE 실제 데이터를 집계한다.
     */
    public static class DatasetSummary {

        private final Long actualFileCount;
        private final Long totalRecordCount;
        private final Long totalFileSizeBytes;

        private final Long hashedFileCount;
        private final Long loadedFileCount;

        public DatasetSummary(
                Long actualFileCount,
                Long totalRecordCount,
                Long totalFileSizeBytes,
                Long hashedFileCount,
                Long loadedFileCount) {

            this.actualFileCount = actualFileCount;
            this.totalRecordCount = totalRecordCount;
            this.totalFileSizeBytes = totalFileSizeBytes;
            this.hashedFileCount = hashedFileCount;
            this.loadedFileCount = loadedFileCount;
        }

        public Long getActualFileCount() {
            return actualFileCount;
        }

        public Long getTotalRecordCount() {
            return totalRecordCount;
        }

        public Long getTotalFileSizeBytes() {
            return totalFileSizeBytes;
        }

        public Long getHashedFileCount() {
            return hashedFileCount;
        }

        public Long getLoadedFileCount() {
            return loadedFileCount;
        }
    }


    /**
     * DATASET_FILE 한 행.
     */
    public static class DatasetFileInfo {

        private final Long datasetFileId;

        private final String fileType;
        private final String originalFileName;

        private final Long fileSizeBytes;
        private final Long recordCount;

        private final String contentHash;

        private final String hdfsRawPath;
        private final String hdfsCleanPath;

        private final String loadStatus;

        public DatasetFileInfo(
                Long datasetFileId,
                String fileType,
                String originalFileName,
                Long fileSizeBytes,
                Long recordCount,
                String contentHash,
                String hdfsRawPath,
                String hdfsCleanPath,
                String loadStatus) {

            this.datasetFileId = datasetFileId;
            this.fileType = fileType;
            this.originalFileName = originalFileName;
            this.fileSizeBytes = fileSizeBytes;
            this.recordCount = recordCount;
            this.contentHash = contentHash;
            this.hdfsRawPath = hdfsRawPath;
            this.hdfsCleanPath = hdfsCleanPath;
            this.loadStatus = loadStatus;
        }

        public Long getDatasetFileId() {
            return datasetFileId;
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
}