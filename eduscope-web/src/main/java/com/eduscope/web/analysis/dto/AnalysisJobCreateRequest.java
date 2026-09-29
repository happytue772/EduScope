package com.eduscope.web.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Analysis Job 신규 요청 DTO.
 *
 * 현재 단계에서는 실제 Hadoop 실행이 아니라
 * PENDING Job을 생성한다.
 */
public record AnalysisJobCreateRequest(

    @NotNull
    @Positive
    Long datasetId,

    @NotBlank
    @Size(max = 50)
    String analysisType,

    @NotBlank
    @Size(max = 30)
    String analysisVersion,

    @NotBlank
    @Size(max = 1000)
    String hdfsInputPath,

    @NotBlank
    @Size(max = 1000)
    String hdfsOutputPath

) {
}