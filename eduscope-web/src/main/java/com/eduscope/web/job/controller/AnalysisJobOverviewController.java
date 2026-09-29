package com.eduscope.web.job.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.job.dto.AnalysisJobResponse;
import com.eduscope.web.job.service.AnalysisJobViewService;

/**
 * Analysis Job 운영현황 조회 전용 Controller.
 *
 * 기존 /api/analysis-jobs와 URL을 분리하여
 * 기존 기능과 충돌하지 않는다.
 */
@RestController
@RequestMapping("/api/analysis-job-overview")
public class AnalysisJobOverviewController {

    private final AnalysisJobViewService service;

    public AnalysisJobOverviewController(
            AnalysisJobViewService service) {

        this.service = service;
    }


    /**
     * Dataset 기준 Analysis Job 전체 조회.
     */
    @GetMapping
    public AnalysisJobResponse getOverview(
            @RequestParam Long datasetId) {

        return service.getJobs(datasetId);
    }
}