package com.eduscope.loader.dataset.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * OULAD 데이터셋 자체의 버전/출처 정보를 관리한다.
 */
@Entity
@Table(
    name = "DATASET",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_DATASET_SRC_VER",
            columnNames = {"SOURCE_NAME", "DATASET_VERSION"}
        )
    }
)
public class Dataset {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "dataset_seq"
    )
    @SequenceGenerator(
        name = "dataset_seq",
        sequenceName = "SEQ_DATASET_ID",
        allocationSize = 1
    )
    @Column(name = "DATASET_ID")
    private Long datasetId;

    @Column(name = "DISPLAY_NAME", nullable = false, length = 150)
    private String displayName;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "SOURCE_TYPE", nullable = false, length = 20)
    private String sourceType;

    @Column(name = "SOURCE_NAME", nullable = false, length = 200)
    private String sourceName;

    @Column(name = "SOURCE_URL", length = 1000)
    private String sourceUrl;

    @Column(name = "DATASET_VERSION", nullable = false, length = 30)
    private String datasetVersion;

    @Column(name = "SCHEMA_VERSION", nullable = false, length = 30)
    private String schemaVersion;

    @Column(name = "HDFS_BASE_PATH", length = 1000)
    private String hdfsBasePath;

    @Column(name = "FILE_COUNT", nullable = false)
    private Integer fileCount;

    @Column(name = "IS_DELETED", nullable = false, length = 1)
    private String isDeleted;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public Long getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(Long datasetId) {
		this.datasetId = datasetId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getDatasetVersion() {
		return datasetVersion;
	}

	public void setDatasetVersion(String datasetVersion) {
		this.datasetVersion = datasetVersion;
	}

	public String getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public String getHdfsBasePath() {
		return hdfsBasePath;
	}

	public void setHdfsBasePath(String hdfsBasePath) {
		this.hdfsBasePath = hdfsBasePath;
	}

	public Integer getFileCount() {
		return fileCount;
	}

	public void setFileCount(Integer fileCount) {
		this.fileCount = fileCount;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
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

	@Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    public Dataset() {
        // JPA 기본 생성자
    }

    // Eclipse:
    // Source → Generate Getters and Setters
}