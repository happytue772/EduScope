package com.eduscope.web.analysis.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.VleActivityStatResponse;
import com.eduscope.web.analysis.service.VleActivityStatService;

/**
 * VLE_ACTIVITY_STAT REST API.
 */
@RestController
@RequestMapping("/api/analysis/vle-activity")
public class VleActivityStatController {

    private final VleActivityStatService service;

    public VleActivityStatController(
            VleActivityStatService service) {

        this.service = service;
    }

    /**
     * 전체 VLE 활동통계.
     */
    @GetMapping
    public Page<VleActivityStatResponse> getStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStats(page, size);
    }

    /**
     * 특정 분석 Job 결과.
     */
    @GetMapping("/job/{jobId}")
    public Page<VleActivityStatResponse> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByJob(
            jobId,
            page,
            size
        );
    }

    /**
     * 특정 VLE 자료의 통계.
     */
    @GetMapping("/material/{vleMaterialId}")
    public Page<VleActivityStatResponse> getByMaterial(
            @PathVariable Long vleMaterialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByMaterial(
            vleMaterialId,
            page,
            size
        );
    }
}