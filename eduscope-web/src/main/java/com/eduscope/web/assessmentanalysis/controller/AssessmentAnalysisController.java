package com.eduscope.web.assessmentanalysis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.assessmentanalysis.dto.AssessmentAnalysisResponse;
import com.eduscope.web.assessmentanalysis.service.AssessmentAnalysisService;

/**
 * 평가 분석 REST API.
 */
@RestController
@RequestMapping("/api/assessment-analysis")
public class AssessmentAnalysisController {

    private final AssessmentAnalysisService service;

    public AssessmentAnalysisController(
            AssessmentAnalysisService service) {

        this.service = service;
    }


    @GetMapping("/{coursePresentationId}")
    public AssessmentAnalysisResponse getAnalysis(
            @PathVariable Long coursePresentationId) {

        return service.getAnalysis(
            coursePresentationId
        );
    }
}