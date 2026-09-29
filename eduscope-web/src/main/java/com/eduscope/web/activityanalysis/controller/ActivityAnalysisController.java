package com.eduscope.web.activityanalysis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.activityanalysis.dto.ActivityAnalysisResponse;
import com.eduscope.web.activityanalysis.service.ActivityAnalysisService;

/**
 * VLE 학습활동 분석 REST API.
 */
@RestController
@RequestMapping("/api/activity-analysis")
public class ActivityAnalysisController {

    private final ActivityAnalysisService service;

    public ActivityAnalysisController(
            ActivityAnalysisService service) {

        this.service = service;
    }

    @GetMapping("/{coursePresentationId}")
    public ActivityAnalysisResponse getAnalysis(
            @PathVariable Long coursePresentationId) {

        return service.getAnalysis(
            coursePresentationId
        );
    }
}