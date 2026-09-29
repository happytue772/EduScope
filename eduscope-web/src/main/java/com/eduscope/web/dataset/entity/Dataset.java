package com.eduscope.web.dataset.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Oracle DATASET 테이블과 매핑되는 JPA Entity.
 */
@Entity
@Table(name = "DATASET")
@SequenceGenerator(
    name = "datasetSequence",
    sequenceName = "SEQ_DATASET_ID",
    allocationSize = 1
)
public class Dataset {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "datasetSequence"
    )
    @Column(name = "DATASET_ID", nullable = false)
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
    private Long fileCount;

    @Column(name = "IS_DELETED", nullable = false, length = 1)
    private String isDeleted;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    protected Dataset() {
        // JPA 기본 생성자
    }

    /**
     * 실제 데이터 Dataset 신규 등록.
     */
    public static Dataset createReal(
            String displayName,
            String description,
            String sourceName,
            String sourceUrl,
            String datasetVersion,
            String schemaVersion,
            String hdfsBasePath) {

        LocalDateTime now = LocalDateTime.now();

        Dataset dataset = new Dataset();

        dataset.displayName = displayName;
        dataset.description = description;
        dataset.sourceType = "REAL";
        dataset.sourceName = sourceName;
        dataset.sourceUrl = sourceUrl;
        dataset.datasetVersion = datasetVersion;
        dataset.schemaVersion = schemaVersion;
        dataset.hdfsBasePath = hdfsBasePath;
        dataset.fileCount = 0L;
        dataset.isDeleted = "N";
        dataset.createdAt = now;
        dataset.updatedAt = now;

        return dataset;
    }

    /**
     * 기존 Dataset의 출처 메타정보를 유지하면서
     * 새로운 버전 행을 생성한다.
     */
    public static Dataset createVersionFrom(
            Dataset source,
            String datasetVersion,
            String schemaVersion,
            String hdfsBasePath) {

        return createReal(
            source.getDisplayName(),
            source.getDescription(),
            source.getSourceName(),
            source.getSourceUrl(),
            datasetVersion,
            schemaVersion,
            hdfsBasePath
        );
    }

    /**
     * 실제 DATASET_FILE 개수와 FILE_COUNT를 동기화한다.
     */
    public void syncFileCount(long fileCount) {

        if (fileCount < 0) {
            throw new IllegalArgumentException(
                "fileCount는 0 이상이어야 합니다."
            );
        }

        this.fileCount = fileCount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 물리 삭제 대신 논리 삭제한다.
     */
    public void markDeleted() {
        this.isDeleted = "Y";
        this.updatedAt = LocalDateTime.now();
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

    public String getIsDeleted() {
        return isDeleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /*
     * 기존 코드 호환을 위해 Setter는 유지한다.
     */
    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setDatasetVersion(String datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public void setHdfsBasePath(String hdfsBasePath) {
        this.hdfsBasePath = hdfsBasePath;
    }

    public void setFileCount(Long fileCount) {
        this.fileCount = fileCount;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
