package com.eduscope.web.analysis.dto;

import com.eduscope.web.analysis.entity.DataQualityStat;

/**
 * 데이터 품질 통계 React 응답 DTO.
 */
public class DataQualityStatResponse {

    private final Long jobId;

    private final Long datasetFileId;
    private final String fileType;
    private final String originalFileName;

    private final String qualityType;
    private final Long recordCount;
    private final String sampleMessage;

    public DataQualityStatResponse(
            Long jobId,
            Long datasetFileId,
            String fileType,
            String originalFileName,
            String qualityType,
            Long recordCount,
            String sampleMessage) {

        this.jobId = jobId;
        this.datasetFileId = datasetFileId;
        this.fileType = fileType;
        this.originalFileName = originalFileName;
        this.qualityType = qualityType;
        this.recordCount = recordCount;
        this.sampleMessage = sampleMessage;
    }

    public static DataQualityStatResponse from(
            DataQualityStat stat) {

        return new DataQualityStatResponse(
            stat.getJobId(),
            stat.getDatasetFileId(),

            stat.getDatasetFile().getFileType(),
            stat.getDatasetFile().getOriginalFileName(),

            stat.getQualityType(),
            stat.getRecordCount(),
            stat.getSampleMessage()
        );
    }

    public Long getJobId() {
        return jobId;
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