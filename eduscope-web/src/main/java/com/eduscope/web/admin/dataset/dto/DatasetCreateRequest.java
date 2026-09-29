package com.eduscope.web.admin.dataset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dataset 신규 등록 요청.
 *
 * 실제 CSV 파일 업로드가 아니라
 * Dataset 메타정보를 등록한다.
 */
public record DatasetCreateRequest(

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

    @NotBlank
    @Size(max = 30)
    String datasetVersion,

    @NotBlank
    @Size(max = 30)
    String schemaVersion,

    @Size(max = 1000)
    String hdfsBasePath

) {
}