package com.eduscope.web.analysis.dto;

import java.time.LocalDateTime;

import com.eduscope.web.analysis.entity.AnalysisJob;

/**
 * React에 전달할 분석 실행이력 DTO.
 */
public class AnalysisJobResponse {

    private final Long jobId;
    private final Long datasetId;

    private final String analysisType;
    private final String status;
    private final String analysisVersion;

    private final String hdfsInputPath;
    private final String hdfsOutputPath;
    private final String resultFilePath;

    private final Long outputRecordCount;
    private final Long invalidRecordCount;
    private final Long duplicateRecordCount;

    private final String resultImportedYn;

    private final LocalDateTime requestedAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;

    public AnalysisJobResponse(
            Long jobId,
            Long datasetId,
            String analysisType,
            String status,
            String analysisVersion,
            String hdfsInputPath,
            String hdfsOutputPath,
            String resultFilePath,
            Long outputRecordCount,
            Long invalidRecordCount,
            Long duplicateRecordCount,
            String resultImportedYn,
            LocalDateTime requestedAt,
            LocalDateTime startedAt,
            LocalDateTime finishedAt) {

        this.jobId = jobId;
        this.datasetId = datasetId;
        this.analysisType = analysisType;
        this.status = status;
        this.analysisVersion = analysisVersion;
        this.hdfsInputPath = hdfsInputPath;
        this.hdfsOutputPath = hdfsOutputPath;
        this.resultFilePath = resultFilePath;
        this.outputRecordCount = outputRecordCount;
        this.invalidRecordCount = invalidRecordCount;
        this.duplicateRecordCount = duplicateRecordCount;
        this.resultImportedYn = resultImportedYn;
        this.requestedAt = requestedAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static AnalysisJobResponse from(
            AnalysisJob job) {

        return new AnalysisJobResponse(
            job.getJobId(),
            job.getDataset().getDatasetId(),
            job.getAnalysisType(),
            job.getStatus(),
            job.getAnalysisVersion(),
            job.getHdfsInputPath(),
            job.getHdfsOutputPath(),
            job.getResultFilePath(),
            job.getOutputRecordCount(),
            job.getInvalidRecordCount(),
            job.getDuplicateRecordCount(),
            job.getResultImportedYn(),
            job.getRequestedAt(),
            job.getStartedAt(),
            job.getFinishedAt()
        );
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public String getStatus() {
        return status;
    }

    public String getAnalysisVersion() {
        return analysisVersion;
    }

    public String getHdfsInputPath() {
        return hdfsInputPath;
    }

    public String getHdfsOutputPath() {
        return hdfsOutputPath;
    }

    public String getResultFilePath() {
        return resultFilePath;
    }

    public Long getOutputRecordCount() {
        return outputRecordCount;
    }

    public Long getInvalidRecordCount() {
        return invalidRecordCount;
    }

    public Long getDuplicateRecordCount() {
        return duplicateRecordCount;
    }

    public String getResultImportedYn() {
        return resultImportedYn;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }
}