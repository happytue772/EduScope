package com.eduscope.web.analysis.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import com.eduscope.web.user.entity.AppUser;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

import com.eduscope.web.dataset.entity.Dataset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * MapReduce 분석 실행 이력 Entity.
 * Oracle ANALYSIS_JOB 테이블과 연결한다.
 */
@Entity
@Table(name = "ANALYSIS_JOB")
public class AnalysisJob {

	@Id
	@GeneratedValue(
	    strategy = GenerationType.SEQUENCE,
	    generator = "analysis_job_seq"
	)
	@SequenceGenerator(
	    name = "analysis_job_seq",
	    sequenceName = "SEQ_ANALYSIS_JOB_ID",
	    allocationSize = 1
	)
	@Column(name = "JOB_ID", nullable = false)
	private Long jobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATASET_ID", nullable = false)
    private Dataset dataset;

    /**
     * 요청 사용자.
     * APP_USER 연결은 Security 단계에서 추가할 예정이므로
     * 현재는 FK 값만 조회한다.
     */
    @Column(name = "REQUESTED_BY")
    private Long requestedBy;
    /**
     * 분석 실행을 요청한 EduScope 사용자.
     *
     * requestedBy 숫자 FK는 그대로 유지하고,
     * APP_USER 조회용 관계만 읽기 전용으로 추가한다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "REQUESTED_BY",
        insertable = false,
        updatable = false
    )
    private AppUser requestedByUser;

    public AppUser getRequestedByUser() {
		return requestedByUser;
	}

	public void setRequestedByUser(AppUser requestedByUser) {
		this.requestedByUser = requestedByUser;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public void setRequestedBy(Long requestedBy) {
		this.requestedBy = requestedBy;
	}

	public void setAnalysisType(String analysisType) {
		this.analysisType = analysisType;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setAnalysisVersion(String analysisVersion) {
		this.analysisVersion = analysisVersion;
	}

	public void setHdfsInputPath(String hdfsInputPath) {
		this.hdfsInputPath = hdfsInputPath;
	}

	public void setHdfsOutputPath(String hdfsOutputPath) {
		this.hdfsOutputPath = hdfsOutputPath;
	}

	public void setResultFilePath(String resultFilePath) {
		this.resultFilePath = resultFilePath;
	}

	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}

	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}

	public void setFinishedAt(LocalDateTime finishedAt) {
		this.finishedAt = finishedAt;
	}

	public void setInputRecordCount(Long inputRecordCount) {
		this.inputRecordCount = inputRecordCount;
	}

	public void setOutputRecordCount(Long outputRecordCount) {
		this.outputRecordCount = outputRecordCount;
	}

	public void setValidRecordCount(Long validRecordCount) {
		this.validRecordCount = validRecordCount;
	}

	public void setInvalidRecordCount(Long invalidRecordCount) {
		this.invalidRecordCount = invalidRecordCount;
	}

	public void setDuplicateRecordCount(Long duplicateRecordCount) {
		this.duplicateRecordCount = duplicateRecordCount;
	}

	public void setProcessingTimeMs(Long processingTimeMs) {
		this.processingTimeMs = processingTimeMs;
	}

	public void setErrorStep(String errorStep) {
		this.errorStep = errorStep;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setResultImportedYn(String resultImportedYn) {
		this.resultImportedYn = resultImportedYn;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Column(name = "ANALYSIS_TYPE", nullable = false, length = 50)
    private String analysisType;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "ANALYSIS_VERSION", nullable = false, length = 30)
    private String analysisVersion;

    @Column(name = "HDFS_INPUT_PATH", nullable = false, length = 1000)
    private String hdfsInputPath;

    @Column(name = "HDFS_OUTPUT_PATH", nullable = false, length = 1000)
    private String hdfsOutputPath;

    @Column(name = "RESULT_FILE_PATH", length = 1000)
    private String resultFilePath;

    @Column(name = "REQUESTED_AT", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    @Column(name = "FINISHED_AT")
    private LocalDateTime finishedAt;

    @Column(name = "INPUT_RECORD_COUNT", nullable = false)
    private Long inputRecordCount;

    @Column(name = "OUTPUT_RECORD_COUNT", nullable = false)
    private Long outputRecordCount;

    @Column(name = "VALID_RECORD_COUNT", nullable = false)
    private Long validRecordCount;

    @Column(name = "INVALID_RECORD_COUNT", nullable = false)
    private Long invalidRecordCount;

    @Column(name = "DUPLICATE_RECORD_COUNT", nullable = false)
    private Long duplicateRecordCount;

    @Column(name = "PROCESSING_TIME_MS")
    private Long processingTimeMs;

    @Column(name = "ERROR_STEP")
    private String errorStep;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "RESULT_IMPORTED_YN", nullable = false, length = 1)
    private String resultImportedYn;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected AnalysisJob() {
        // JPA 기본 생성자
    }
    public static AnalysisJob createPending(
            Dataset dataset,
            Long requestedBy,
            String analysisType,
            String analysisVersion,
            String hdfsInputPath,
            String hdfsOutputPath) {

        LocalDateTime now =
            LocalDateTime.now();


        AnalysisJob job =
            new AnalysisJob();


        job.dataset =
            dataset;

        job.requestedBy =
            requestedBy;

        job.analysisType =
            analysisType;

        job.status =
            "PENDING";

        job.analysisVersion =
            analysisVersion;

        job.hdfsInputPath =
            hdfsInputPath;

        job.hdfsOutputPath =
            hdfsOutputPath;

        job.resultFilePath =
            null;

        job.requestedAt =
            now;

        job.startedAt =
            null;

        job.finishedAt =
            null;


        /*
         * 실제 실행 전이므로 처리건수는 0.
         */
        job.inputRecordCount =
            0L;

        job.outputRecordCount =
            0L;

        job.validRecordCount =
            0L;

        job.invalidRecordCount =
            0L;

        job.duplicateRecordCount =
            0L;


        job.processingTimeMs =
            null;

        job.errorStep =
            null;

        job.errorMessage =
            null;


        job.resultImportedYn =
            "N";

        job.createdAt =
            now;


        return job;
    }

    public Long getJobId() {
        return jobId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public Long getRequestedBy() {
        return requestedBy;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void markRunning() {

        if (
            !"PENDING".equals(
                this.status
            )
        ) {

            throw new IllegalStateException(
                "PENDING Job만 실행할 수 있습니다."
            );
        }


        this.status =
            "RUNNING";

        this.startedAt =
            LocalDateTime.now();

        this.finishedAt =
            null;

        this.processingTimeMs =
            null;

        this.errorStep =
            null;

        this.errorMessage =
            null;
    }


    /**
     * 실제 MapReduce 성공.
     */
    public void markSuccess(
            long outputRecordCount,
            long processingTimeMs) {

        if (
            !"RUNNING".equals(
                this.status
            )
        ) {

            throw new IllegalStateException(
                "RUNNING Job만 SUCCESS 처리할 수 있습니다."
            );
        }


        this.status =
            "SUCCESS";

        this.finishedAt =
            LocalDateTime.now();

        this.outputRecordCount =
            outputRecordCount;

        this.processingTimeMs =
            processingTimeMs;

        this.errorStep =
            null;

        this.errorMessage =
            null;


        /*
         * MapReduce 성공과
         * Oracle Batch 적재 성공은 별개다.
         */
        this.resultImportedYn =
            "N";
    }


    /**
     * 실제 MapReduce 실패.
     */
    public void markFailed(
            String errorStep,
            String errorMessage,
            long processingTimeMs) {

        this.status =
            "FAILED";

        this.finishedAt =
            LocalDateTime.now();

        this.processingTimeMs =
            processingTimeMs;

        this.errorStep =
            truncate(
                errorStep,
                100
            );

        this.errorMessage =
            truncate(
                errorMessage,
                2000
            );

        this.resultImportedYn =
            "N";
    }


    /**
     * Oracle Column 길이를 넘지 않도록 제한.
     */
    private String truncate(
            String value,
            int maxLength) {

        if (
            value == null
        ) {

            return null;
        }


        if (
            value.length()
            <= maxLength
        ) {

            return value;
        }


        return value.substring(
            0,
            maxLength
        );
    }
}