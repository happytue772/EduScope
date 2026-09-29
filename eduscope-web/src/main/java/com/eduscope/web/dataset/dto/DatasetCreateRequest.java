package com.eduscope.web.dataset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 신규 Dataset 등록 요청 DTO.
 *
 * 현재 EduScope는 실제 데이터만 사용하므로
 * sourceType은 Service에서 REAL로 고정한다.
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
