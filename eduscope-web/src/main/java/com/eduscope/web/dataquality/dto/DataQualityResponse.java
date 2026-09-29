package com.eduscope.web.dataquality.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Quality 분석 화면 응답 DTO.
 *
 * DATASET
 * DATASET_FILE
 * ANALYSIS_JOB
 * DATA_QUALITY_STAT
 * 데이터를 조합한다.
 */
public class DataQualityResponse {

    private final DatasetInfo dataset;
    private final JobInfo job;
    private final Summary summary;
    private final List<QualityItem> items;

    public DataQualityResponse(
            DatasetInfo dataset,
            JobInfo job,
            Summary summary,
            List<QualityItem> items) {

        this.dataset = dataset;
        this.job = job;
        this.summary = summary;
        this.items = items;
    }

    public DatasetInfo getDataset() {
        return dataset;
    }

    public JobInfo getJob() {
        return job;
    }

    public Summary getSummary() {
        return summary;
    }

    public List<QualityItem> getItems() {
        return items;
    }


    /**
     * Dataset 기본정보.
     */
    public static class DatasetInfo {

        private final Long datasetId;
        private final String displayName;
        private final String datasetVersion;
        private final String schemaVersion;
        private final Long fileCount;
        private final String hdfsBasePath;

        public DatasetInfo(
                Long datasetId,
                String displayName,
                String datasetVersion,
                String schemaVersion,
                Long fileCount,
                String hdfsBasePath) {

            this.datasetId = datasetId;
            this.displayName = displayName;
            this.datasetVersion = datasetVersion;
            this.schemaVersion = schemaVersion;
            this.fileCount = fileCount;
            this.hdfsBasePath = hdfsBasePath;
        }

        public Long getDatasetId() {
            return datasetId;
        }

        public String getDisplayName() {
            return displayName;
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

        public String getHdfsBasePath() {
            return hdfsBasePath;
        }
    }


    /**
     * DATA_QUALITY ANALYSIS_JOB 정보.
     */
    public static class JobInfo {

        private final Long jobId;
        private final String status;

        private final Long outputRecordCount;
        private final Long duplicateRecordCount;

        private final String resultImportedYn;
        private final String hdfsOutputPath;

        private final LocalDateTime createdAt;

        public JobInfo(
                Long jobId,
                String status,
                Long outputRecordCount,
                Long duplicateRecordCount,
                String resultImportedYn,
                String hdfsOutputPath,
                LocalDateTime createdAt) {

            this.jobId = jobId;
            this.status = status;
            this.outputRecordCount = outputRecordCount;
            this.duplicateRecordCount = duplicateRecordCount;
            this.resultImportedYn = resultImportedYn;
            this.hdfsOutputPath = hdfsOutputPath;
            this.createdAt = createdAt;
        }

        public Long getJobId() {
            return jobId;
        }

        public String getStatus() {
            return status;
        }

        public Long getOutputRecordCount() {
            return outputRecordCount;
        }

        public Long getDuplicateRecordCount() {
            return duplicateRecordCount;
        }

        public String getResultImportedYn() {
            return resultImportedYn;
        }

        public String getHdfsOutputPath() {
            return hdfsOutputPath;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }


    /**
     * 화면 KPI 요약.
     */
    public static class Summary {

        private final Long totalIssueRecordCount;
        private final Long affectedFileCount;
        private final Long qualityTypeCount;

        public Summary(
                Long totalIssueRecordCount,
                Long affectedFileCount,
                Long qualityTypeCount) {

            this.totalIssueRecordCount =
                totalIssueRecordCount;

            this.affectedFileCount =
                affectedFileCount;

            this.qualityTypeCount =
                qualityTypeCount;
        }

        public Long getTotalIssueRecordCount() {
            return totalIssueRecordCount;
        }

        public Long getAffectedFileCount() {
            return affectedFileCount;
        }

        public Long getQualityTypeCount() {
            return qualityTypeCount;
        }
    }


    /**
     * DATA_QUALITY_STAT 한 행.
     */
    public static class QualityItem {

        private final Long datasetFileId;

        private final String fileType;
        private final String originalFileName;

        private final Long fileRecordCount;
        private final String loadStatus;

        private final String qualityType;
        private final Long recordCount;
        private final String sampleMessage;

        public QualityItem(
                Long datasetFileId,
                String fileType,
                String originalFileName,
                Long fileRecordCount,
                String loadStatus,
                String qualityType,
                Long recordCount,
                String sampleMessage) {

            this.datasetFileId = datasetFileId;
            this.fileType = fileType;
            this.originalFileName = originalFileName;
            this.fileRecordCount = fileRecordCount;
            this.loadStatus = loadStatus;
            this.qualityType = qualityType;
            this.recordCount = recordCount;
            this.sampleMessage = sampleMessage;
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

        public Long getFileRecordCount() {
            return fileRecordCount;
        }

        public String getLoadStatus() {
            return loadStatus;
        }

        public String getQualityType() {
            return qualityType;
        }

        public Long getRecordCount() {
            return recordCount;
        }

        public String getSampleMessage() {
            return sampleMessage;
        }
    }
}