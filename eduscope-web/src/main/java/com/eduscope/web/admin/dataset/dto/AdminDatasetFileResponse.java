package com.eduscope.web.admin.dataset.dto;

import java.time.LocalDateTime;

/**
 * Dataset에 포함된 실제 원본 파일 정보.
 */
public record AdminDatasetFileResponse(

    Long datasetFileId,
    Long datasetId,

    String fileType,
    String originalFileName,

    Long fileSizeBytes,
    Long recordCount,

    String contentHash,

    String hdfsRawPath,
    String hdfsCleanPath,

    String loadStatus,

    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {
}