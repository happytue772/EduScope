package com.eduscope.web.dataset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DATASET_FILE SHA-256 Checksum 갱신 요청 DTO.
 */
public record DatasetFileChecksumRequest(

    @NotBlank
    @Size(min = 64, max = 64)
    String contentHash

) {
}
