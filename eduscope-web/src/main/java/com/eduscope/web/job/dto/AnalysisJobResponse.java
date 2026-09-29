package com.eduscope.web.job.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Analysis Job 관리 화면 응답.
 */
public class AnalysisJobResponse {

    private final Summary summary;
    private final List<JobItem> jobs;

    public AnalysisJobResponse(
            Summary summary,
            List<JobItem> jobs) {

        this.summary = summary;
        this.jobs = jobs;
    }

    public Summary getSummary() {
        return summary;
    }

    public List<JobItem> getJobs() {
        return jobs;
    }


    public static class Summary {

        private final Long totalJobCount;
        private final Long successCount;
        private final Long failedCount;
        private final Long importedCount;

        public Summary(
                Long totalJobCount,
                Long successCount,
                Long failedCount,
                Long importedCount) {

            this.totalJobCount = totalJobCount;
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.importedCount = importedCount;
        }

        public Long getTotalJobCount() {
            return totalJobCount;
        }

        public Long getSuccessCount() {
            return successCount;
        }

        public Long getFailedCount() {
            return failedCount;
        }

        public Long getImportedCount() {
            return importedCount;
        }
    }


    public static class JobItem {

        private final Long jobId;
        private final String analysisType;
        private final String status;
        private final String analysisVersion;

        private final String hdfsInputPath;
        private final String hdfsOutputPath;
        private final String resultFilePath;

        private final LocalDateTime requestedAt;
        private final LocalDateTime startedAt;
        private final LocalDateTime finishedAt;

        private final Long inputRecordCount;
        private final Long outputRecordCount;
        private final Long validRecordCount;
        private final Long invalidRecordCount;
        private final Long duplicateRecordCount;

        private final Long processingTimeMs;

        private final String errorStep;
        private final String errorMessage;

        private final String resultImportedYn;

        public JobItem(
                Long jobId,
                String analysisType,
                String status,
                String analysisVersion,
                String hdfsInputPath,
                String hdfsOutputPath,
                String resultFilePath,
                LocalDateTime requestedAt,
                LocalDateTime startedAt,
                LocalDateTime finishedAt,
                Long inputRecordCount,
                Long outputRecordCount,
                Long validRecordCount,
                Long invalidRecordCount,
                Long duplicateRecordCount,
                Long processingTimeMs,
                String errorStep,
                String errorMessage,
                String resultImportedYn) {

            this.jobId = jobId;
            this.analysisType = analysisType;
            this.status = status;
            this.analysisVersion = analysisVersion;
            this.hdfsInputPath = hdfsInputPath;
            this.hdfsOutputPath = hdfsOutputPath;
            this.resultFilePath = resultFilePath;
            this.requestedAt = requestedAt;
            this.startedAt = startedAt;
            this.finishedAt = finishedAt;
            this.inputRecordCount = inputRecordCount;
            this.outputRecordCount = outputRecordCount;
            this.validRecordCount = validRecordCount;
            this.invalidRecordCount = invalidRecordCount;
            this.duplicateRecordCount = duplicateRecordCount;
            this.processingTimeMs = processingTimeMs;
            this.errorStep = errorStep;
            this.errorMessage = errorMessage;
            this.resultImportedYn = resultImportedYn;
        }

        public Long getJobId() {
            return jobId;
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

        public LocalDateTime getRequestedAt() {
            return requestedAt;
        }

        public LocalDateTime getStartedAt() {
            return startedAt;
        }

        public LocalDateTime getFinishedAt() {
            return finishedAt;
        }

        public Long getInputRecordCount() {
            return inputRecordCount;
        }

        public Long getOutputRecordCount() {
            return outputRecordCount;
        }

        public Long getValidRecordCount() {
            return validRecordCount;
        }

        public Long getInvalidRecordCount() {
            return invalidRecordCount;
        }

        public Long getDuplicateRecordCount() {
            return duplicateRecordCount;
        }

        public Long getProcessingTimeMs() {
            return processingTimeMs;
        }

        public String getErrorStep() {
            return errorStep;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getResultImportedYn() {
            return resultImportedYn;
        }
    }
}