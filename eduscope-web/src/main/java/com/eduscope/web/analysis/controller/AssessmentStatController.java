package com.eduscope.web.analysis.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.AssessmentStatResponse;
import com.eduscope.web.analysis.service.AssessmentStatService;

/**
 * ASSESSMENT_STAT REST API.
 */
@RestController
@RequestMapping("/api/analysis/assessment")
public class AssessmentStatController {

    private final AssessmentStatService service;

    public AssessmentStatController(
            AssessmentStatService service) {

        this.service = service;
    }

    /**
     * 전체 평가 통계.
     */
    @GetMapping
    public Page<AssessmentStatResponse> getStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStats(
            page,
            size
        );
    }

    /**
     * 특정 분석 Job 통계.
     */
    @GetMapping("/job/{jobId}")
    public Page<AssessmentStatResponse> getByJob(
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
     * 특정 평가 통계.
     */
    @GetMapping("/assessment/{assessmentId}")
    public Page<AssessmentStatResponse> getByAssessment(
            @PathVariable Long assessmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByAssessment(
            assessmentId,
            page,
            size
        );
    }
}