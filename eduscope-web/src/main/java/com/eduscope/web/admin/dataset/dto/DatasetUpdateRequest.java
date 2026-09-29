package com.eduscope.web.admin.dataset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dataset 메타정보 수정 요청.
 *
 * datasetVersion / schemaVersion은
 * 기존 분석 재현성을 위해 수정하지 않는다.
 *
 * 새로운 Version은 신규 Dataset으로 등록한다.
 */
public record DatasetUpdateRequest(

    @NotBlank
    @Size(max = 150)
    String displayName,

    @Size(max = 1000)
    String description,

    @NotBlank
    @Size(max = 200)
    String sourceName,

    @Size(max = 1000)
    String sourceUrl,

    @Size(max = 1000)
    String hdfsBasePath

) {
}