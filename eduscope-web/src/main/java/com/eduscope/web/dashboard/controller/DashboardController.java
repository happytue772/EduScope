package com.eduscope.web.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.dashboard.dto.DashboardSummaryResponse;
import com.eduscope.web.dashboard.service.DashboardService;

/**
 * EduScope Dashboard REST API.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(
            DashboardService service) {

        this.service = service;
    }

    /**
     * Dashboard 핵심 KPI 조회.
     */
    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(
            @RequestParam Long datasetId) {

        return service.getSummary(
            datasetId
        );
    }
}