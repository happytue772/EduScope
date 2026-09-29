package com.eduscope.web.resultanalysis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.resultanalysis.dto.ResultAnalysisResponse;
import com.eduscope.web.resultanalysis.service.ResultAnalysisService;

/**
 * 최종성과 비교 REST API.
 */
@RestController
@RequestMapping("/api/result-analysis")
public class ResultAnalysisController {

    private final ResultAnalysisService service;

    public ResultAnalysisController(
            ResultAnalysisService service) {

        this.service = service;
    }

    @GetMapping("/{coursePresentationId}")
    public ResultAnalysisResponse getAnalysis(
            @PathVariable Long coursePresentationId) {

        return service.getAnalysis(
            coursePresentationId
        );
    }
}