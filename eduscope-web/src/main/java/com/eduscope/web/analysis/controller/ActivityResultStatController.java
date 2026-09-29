package com.eduscope.web.analysis.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.ActivityResultStatResponse;
import com.eduscope.web.analysis.service.ActivityResultStatService;

/**
 * ACTIVITY_RESULT_STAT REST API.
 */
@RestController
@RequestMapping("/api/analysis/activity-result")
public class ActivityResultStatController {

    private final ActivityResultStatService service;

    public ActivityResultStatController(
            ActivityResultStatService service) {

        this.service = service;
    }

    @GetMapping
    public Page<ActivityResultStatResponse> getStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStats(page, size);
    }

    @GetMapping("/job/{jobId}")
    public Page<ActivityResultStatResponse> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByJob(jobId, page, size);
    }

    @GetMapping("/course/{coursePresentationId}")
    public Page<ActivityResultStatResponse> getByCourse(
            @PathVariable Long coursePresentationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByCourse(
            coursePresentationId,
            page,
            size
        );
    }

    @GetMapping("/result/{finalResult}")
    public Page<ActivityResultStatResponse> getByResult(
            @PathVariable String finalResult,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByResult(
            finalResult,
            page,
            size
        );
    }
}