package com.eduscope.web.dataset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * DATASET_FILE 메타정보 등록 요청 DTO.
 *
 * contentHash는 실제 파일에서 계산한 SHA-256 값을 전달한다.
 * 임의 해시는 생성하지 않는다.
 */
public record DatasetFileCreateRequest(

    @NotBlank
    @Size(max = 40)
    String fileType,

    @NotBlank
    @Size(max = 255)
    String originalFileName,

    @PositiveOrZero
    Long fileSizeBytes,

    @PositiveOrZero
    Long recordCount,

    @Size(max = 64)
    String contentHash,

    @Size(max = 1000)
    String hdfsRawPath,

    @Size(max = 1000)
    String hdfsCleanPath,

    @Size(max = 30)
    String loadStatus

) {
}
