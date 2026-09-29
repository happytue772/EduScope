package com.eduscope.web.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * FAILED Job 재실행 요청.
 *
 * 기존 분석 설정은 유지하고
 * 새로운 HDFS Output 경로만 받는다.
 */
public record AnalysisJobRetryRequest(

    @NotBlank
    @Size(max = 1000)
    String hdfsOutputPath

) {
}