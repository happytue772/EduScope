package com.eduscope.web.system.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.system.dto.SystemHealthResponse;
import com.eduscope.web.system.service.SystemHealthService;


/**
 * ADMIN 전용 시스템 상태 API.
 *
 * URL 기반 SecurityConfig와
 * Method Security를 이중 적용한다.
 */
@RestController
@RequestMapping(
    "/api/admin/system-health"
)
@PreAuthorize(
    "hasAnyRole('DEMO_ADMIN', 'ADMIN')"
)
public class SystemHealthController {

    private final SystemHealthService service;


    public SystemHealthController(
            SystemHealthService service) {

        this.service =
            service;
    }


    /**
     * Application / Oracle / JVM /
     * 최신 Analysis Job 상태 조회.
     */
    @GetMapping
    public SystemHealthResponse
            getSystemHealth() {

        return service
            .getSystemHealth();
    }
}
