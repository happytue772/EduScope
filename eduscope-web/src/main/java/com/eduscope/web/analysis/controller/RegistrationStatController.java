package com.eduscope.web.analysis.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.RegistrationStatResponse;
import com.eduscope.web.analysis.service.RegistrationStatService;

/**
 * REGISTRATION_STAT REST API.
 */
@RestController
@RequestMapping("/api/analysis/registration")
public class RegistrationStatController {

    private final RegistrationStatService service;

    public RegistrationStatController(
            RegistrationStatService service) {

        this.service = service;
    }

    @GetMapping
    public Page<RegistrationStatResponse> getStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStats(page, size);
    }

    @GetMapping("/job/{jobId}")
    public Page<RegistrationStatResponse> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByJob(jobId, page, size);
    }

    @GetMapping("/course/{coursePresentationId}")
    public Page<RegistrationStatResponse> getByCourse(
            @PathVariable Long coursePresentationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByCourse(
            coursePresentationId,
            page,
            size
        );
    }
}