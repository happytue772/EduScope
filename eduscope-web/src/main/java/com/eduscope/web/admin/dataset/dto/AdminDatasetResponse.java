package com.eduscope.web.admin.dataset.dto;

import java.time.LocalDateTime;

/**
 * ADMIN Dataset 관리용 응답 DTO.
 */
public record AdminDatasetResponse(

    Long datasetId,

    String displayName,

    String description,

    String sourceType,

    String sourceName,

    String sourceUrl,

    String datasetVersion,

    String schemaVersion,

    String hdfsBasePath,

    Long fileCount,

    String isDeleted,

    LocalDateTime createdAt,

    LocalDateTime updatedAt

) {
}