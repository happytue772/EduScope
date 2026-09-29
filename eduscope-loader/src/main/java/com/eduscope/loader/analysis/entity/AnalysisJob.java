package com.eduscope.loader.analysis.entity;

import java.time.LocalDateTime;

import com.eduscope.loader.dataset.entity.Dataset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Hadoop 분석 실행 이력 및 결과 적재 상태 관리 Entity.
 */
@Entity
@Table(name = "ANALYSIS_JOB")
public class AnalysisJob {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "analysisJobSeq"
    )
    @SequenceGenerator(
        name = "analysisJobSeq",
        sequenceName = "SEQ_ANALYSIS_JOB_ID",
        allocationSize = 1
    )
    @Column(name = "JOB_ID")
    private Long jobId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "DATASET_ID",
        nullable = false
    )
    private Dataset dataset;

    /*
     * 현재 Bootstrap은 자동 적재이므로 NULL.
     * APP_USER와 OULAD 학생은 연결하지 않는다.
     */
    @Column(name = "REQUESTED_BY")
    private Long requestedBy;

    @Column(
        name = "ANALYSIS_TYPE",
        nullable = false,
        length = 50
    )
    private String analysisType;

    @Column(
        name = "STATUS",
        nullable = false,
        length = 20
    )
    private String status;

    @Column(
        name = "ANALYSIS_VERSION",
        nullable = false,
        length = 30
    )
    private String analysisVersion;

    @Column(
        name = "HDFS_INPUT_PATH",
        nullable = false,
        length = 1000
    )
    private String hdfsInputPath;

    @Column(
        name = "HDFS_OUTPUT_PATH",
        nullable = false,
        length = 1000
    )
    private String hdfsOutputPath;

    @Column(
        name = "RESULT_FILE_PATH",
        length = 1000
    )
    private String resultFilePath;

    @Column(
        name = "REQUESTED_AT",
        nullable = false
    )
    private LocalDateTime requestedAt;

    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    @Column(name = "FINISHED_AT")
    private LocalDateTime finishedAt;

    @Column(
        name = "INPUT_RECORD_COUNT",
        nullable = false
    )
    private Long inputRecordCount;

    @Column(
        name = "OUTPUT_RECORD_COUNT",
        nullable = false
    )
    private Long outputRecordCount;

    @Column(
        name = "VALID_RECORD_COUNT",
        nullable = false
    )
    private Long validRecordCount;

    @Column(
        name = "INVALID_RECORD_COUNT",
        nullable = false
    )
    private Long invalidRecordCount;

    @Column(
        name = "DUPLICATE_RECORD_COUNT",
        nullable = false
    )
    private Long duplicateRecordCount;

    @Column(name = "PROCESSING_TIME_MS")
    private Long processingTimeMs;

    @Column(
        name = "ERROR_STEP",
        length = 100
    )
    private String errorStep;

    @Column(
        name = "ERROR_MESSAGE",
        length = 2000
    )
    private String errorMessage;

    @Column(
        name = "RESULT_IMPORTED_YN",
        nullable = false,
        length = 1
    )
    private String resultImportedYn;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public AnalysisJob() {
    }

    public Long getJobId() {
        return jobId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Long getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Long requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAnalysisVersion() {
        return analysisVersion;
    }

    public void setAnalysisVersion(String analysisVersion) {
        this.analysisVersion = analysisVersion;
    }

    public String getHdfsInputPath() {
        return hdfsInputPath;
    }

    public void setHdfsInputPath(String hdfsInputPath) {
        this.hdfsInputPath = hdfsInputPath;
    }

    public String getHdfsOutputPath() {
        return hdfsOutputPath;
    }

    public void setHdfsOutputPath(String hdfsOutputPath) {
        this.hdfsOutputPath = hdfsOutputPath;
    }

    public String getResultFilePath() {
        return resultFilePath;
    }

    public void setResultFilePath(String resultFilePath) {
        this.resultFilePath = resultFilePath;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getInputRecordCount() {
        return inputRecordCount;
    }

    public void setInputRecordCount(Long inputRecordCount) {
        this.inputRecordCount = inputRecordCount;
    }

    public Long getOutputRecordCount() {
        return outputRecordCount;
    }

    public void setOutputRecordCount(Long outputRecordCount) {
        this.outputRecordCount = outputRecordCount;
    }

    public Long getValidRecordCount() {
        return validRecordCount;
    }

    public void setValidRecordCount(Long validRecordCount) {
        this.validRecordCount = validRecordCount;
    }

    public Long getInvalidRecordCount() {
        return invalidRecordCount;
    }

    public void setInvalidRecordCount(Long invalidRecordCount) {
        this.invalidRecordCount = invalidRecordCount;
    }

    public Long getDuplicateRecordCount() {
        return duplicateRecordCount;
    }

    public void setDuplicateRecordCount(Long duplicateRecordCount) {
        this.duplicateRecordCount = duplicateRecordCount;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getErrorStep() {
        return errorStep;
    }

    public void setErrorStep(String errorStep) {
        this.errorStep = errorStep;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getResultImportedYn() {
        return resultImportedYn;
    }

    public void setResultImportedYn(String resultImportedYn) {
        this.resultImportedYn = resultImportedYn;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}