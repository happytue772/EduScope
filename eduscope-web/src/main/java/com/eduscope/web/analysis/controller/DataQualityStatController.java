package com.eduscope.web.analysis.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.DataQualityStatResponse;
import com.eduscope.web.analysis.service.DataQualityStatService;

/**
 * DATA_QUALITY_STAT REST API.
 *
 * 기존 상세/페이지 조회 API만 담당한다.
 * /summary는 기존 dataquality 패키지의
 * DataQualityController가 담당한다.
 */
@RestController
@RequestMapping("/api/analysis/data-quality")
public class DataQualityStatController {

    private final DataQualityStatService service;

    public DataQualityStatController(
            DataQualityStatService service) {

        this.service = service;
    }

    @GetMapping
    public Page<DataQualityStatResponse> getStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStats(page, size);
    }

    @GetMapping("/job/{jobId}")
    public Page<DataQualityStatResponse> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByJob(
            jobId,
            page,
            size
        );
    }

    @GetMapping("/file/{datasetFileId}")
    public Page<DataQualityStatResponse> getByFile(
            @PathVariable Long datasetFileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByFile(
            datasetFileId,
            page,
            size
        );
    }

    @GetMapping("/type/{qualityType}")
    public Page<DataQualityStatResponse> getByQualityType(
            @PathVariable String qualityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByQualityType(
            qualityType,
            page,
            size
        );
    }
}