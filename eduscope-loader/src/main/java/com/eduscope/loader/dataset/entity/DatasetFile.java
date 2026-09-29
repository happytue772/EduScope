package com.eduscope.loader.dataset.entity;

import java.time.LocalDateTime;

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
import jakarta.persistence.UniqueConstraint;

/**
 * OULAD를 구성하는 실제 CSV 파일 정보를 관리한다.
 */
@Entity
@Table(
    name = "DATASET_FILE",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_DS_FILE_TYPE",
            columnNames = {"DATASET_ID", "FILE_TYPE"}
        )
    }
)
public class DatasetFile {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "dataset_file_seq"
    )
    @SequenceGenerator(
        name = "dataset_file_seq",
        sequenceName = "SEQ_DATASET_FILE_ID",
        allocationSize = 1
    )
    @Column(name = "DATASET_FILE_ID")
    private Long datasetFileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DATASET_ID", nullable = false)
    private Dataset dataset;

    @Column(name = "FILE_TYPE", nullable = false, length = 40)
    private String fileType;

    @Column(name = "ORIGINAL_FILE_NAME", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "FILE_SIZE_BYTES")
    private Long fileSizeBytes;

    public Long getDatasetFileId() {
		return datasetFileId;
	}

	public void setDatasetFileId(Long datasetFileId) {
		this.datasetFileId = datasetFileId;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public Long getFileSizeBytes() {
		return fileSizeBytes;
	}

	public void setFileSizeBytes(Long fileSizeBytes) {
		this.fileSizeBytes = fileSizeBytes;
	}

	public Long getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(Long recordCount) {
		this.recordCount = recordCount;
	}

	public String getContentHash() {
		return contentHash;
	}

	public void setContentHash(String contentHash) {
		this.contentHash = contentHash;
	}

	public String getHdfsRawPath() {
		return hdfsRawPath;
	}

	public void setHdfsRawPath(String hdfsRawPath) {
		this.hdfsRawPath = hdfsRawPath;
	}

	public String getHdfsCleanPath() {
		return hdfsCleanPath;
	}

	public void setHdfsCleanPath(String hdfsCleanPath) {
		this.hdfsCleanPath = hdfsCleanPath;
	}

	public String getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(String loadStatus) {
		this.loadStatus = loadStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Column(name = "RECORD_COUNT")
    private Long recordCount;

    @Column(name = "CONTENT_HASH", length = 64)
    private String contentHash;

    @Column(name = "HDFS_RAW_PATH", length = 1000)
    private String hdfsRawPath;

    @Column(name = "HDFS_CLEAN_PATH", length = 1000)
    private String hdfsCleanPath;

    @Column(name = "LOAD_STATUS", nullable = false, length = 30)
    private String loadStatus;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    public DatasetFile() {
        // JPA 기본 생성자
    }

    // Source → Generate Getters and Setters
}