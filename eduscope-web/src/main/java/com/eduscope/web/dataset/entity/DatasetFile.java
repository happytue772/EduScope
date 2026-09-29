package com.eduscope.web.dataset.entity;

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

/**
 * OULAD 원본 파일 관리정보 Entity.
 * Oracle DATASET_FILE 테이블과 연결한다.
 */
@Entity
@Table(name = "DATASET_FILE")
@SequenceGenerator(
    name = "datasetFileSequence",
    sequenceName = "SEQ_DATASET_FILE_ID",
    allocationSize = 1
)
public class DatasetFile {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "datasetFileSequence"
    )
    @Column(name = "DATASET_FILE_ID", nullable = false)
    private Long datasetFileId;

    /**
     * DATASET_FILE.DATASET_ID
     * → DATASET.DATASET_ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATASET_ID", nullable = false)
    private Dataset dataset;

    @Column(name = "FILE_TYPE", nullable = false, length = 40)
    private String fileType;

    @Column(
        name = "ORIGINAL_FILE_NAME",
        nullable = false,
        length = 255
    )
    private String originalFileName;

    @Column(name = "FILE_SIZE_BYTES")
    private Long fileSizeBytes;

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

    protected DatasetFile() {
        // JPA 기본 생성자
    }

    /**
     * 실제 원본 파일 메타정보를 등록한다.
     */
    public static DatasetFile create(
            Dataset dataset,
            String fileType,
            String originalFileName,
            Long fileSizeBytes,
            Long recordCount,
            String contentHash,
            String hdfsRawPath,
            String hdfsCleanPath,
            String loadStatus) {

        LocalDateTime now = LocalDateTime.now();

        DatasetFile file = new DatasetFile();

        file.dataset = dataset;
        file.fileType = fileType;
        file.originalFileName = originalFileName;
        file.fileSizeBytes = fileSizeBytes;
        file.recordCount = recordCount;
        file.contentHash = contentHash;
        file.hdfsRawPath = hdfsRawPath;
        file.hdfsCleanPath = hdfsCleanPath;
        file.loadStatus = loadStatus;
        file.createdAt = now;
        file.updatedAt = now;

        return file;
    }

    /**
     * 실제 파일에서 계산된 SHA-256 값만 갱신한다.
     */
    public void updateContentHash(String contentHash) {
        this.contentHash = contentHash;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getDatasetFileId() {
        return datasetFileId;
    }

    public Dataset getDataset() {
        return dataset;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
