package com.eduscope.web.registrationanalysis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.registrationanalysis.dto.RegistrationAnalysisResponse;
import com.eduscope.web.registrationanalysis.service.RegistrationAnalysisService;

/**
 * 수강/철회 분석 REST API.
 */
@RestController
@RequestMapping("/api/registration-analysis")
public class RegistrationAnalysisController {

    private final RegistrationAnalysisService service;

    public RegistrationAnalysisController(
            RegistrationAnalysisService service) {

        this.service = service;
    }

    @GetMapping("/{coursePresentationId}")
    public RegistrationAnalysisResponse getAnalysis(
            @PathVariable Long coursePresentationId) {

        return service.getAnalysis(
            coursePresentationId
        );
    }
}