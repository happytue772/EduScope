package com.eduscope.web.dataset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 기존 Dataset을 기준으로 새 버전을 등록하는 요청 DTO.
 *
 * 기존 Dataset 행의 버전을 덮어쓰지 않고
 * 새로운 DATASET 행을 생성해 버전 이력을 보존한다.
 */
public record DatasetVersionCreateRequest(

    @NotBlank
    @Size(max = 30)
    String datasetVersion,

    @NotBlank
    @Size(max = 30)
    String schemaVersion,

    @NotBlank
    @Size(max = 1000)
    String hdfsBasePath

) {
}
