package com.eduscope.web.audit.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.audit.dto.AuditLogResponse;
import com.eduscope.web.audit.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 관리자 Audit Log REST API.
 *
 * 전체 Controller가 ADMIN 권한 전용이다.
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

    private final AuditLogService service;


    public AuditLogController(
            AuditLogService service) {

        this.service = service;
    }


    /**
     * Audit Log 페이지 조회.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DEMO_ADMIN', 'ADMIN')")
    public Page<AuditLogResponse> getLogs(
            @RequestParam(
                defaultValue = "0"
            )
            int page,

            @RequestParam(
                defaultValue = "20"
            )
            int size) {

        return service.getLogs(
            page,
            size
        );
    }


    /**
     * Purge 실행 전 실제 삭제 대상 건수 확인.
     *
     * 예:
     * /purge-preview?before=2026-01-01
     *
     * 2026-01-01 00:00 이전 기록을 계산한다.
     */
    @GetMapping("/purge-preview")
    @PreAuthorize("hasAnyRole('DEMO_ADMIN', 'ADMIN')")
    public Map<String, Object> previewPurge(
            @RequestParam
            @DateTimeFormat(
                iso = DateTimeFormat.ISO.DATE
            )
            LocalDate before) {

        LocalDateTime cutoff =
            before.atStartOfDay();


        long deleteCount =
            service.countLogsBefore(
                cutoff
            );


        return Map.of(
            "before",
            before.toString(),
            "deleteCount",
            deleteCount
        );
    }


    /**
     * 특정 날짜 이전 Audit Log 일괄 정리.
     */
    @DeleteMapping("/purge")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> purgeLogs(
            @RequestParam
            @DateTimeFormat(
                iso = DateTimeFormat.ISO.DATE
            )
            LocalDate before,

            Principal principal,

            HttpServletRequest request) {

        LocalDateTime cutoff =
            before.atStartOfDay();


        int deletedCount =
            service.purgeLogsBefore(
                cutoff,
                principal.getName(),
                request.getRequestURI(),
                request.getRemoteAddr()
            );


        return Map.of(
            "message",
            "Audit Log 정리가 완료되었습니다.",
            "before",
            before.toString(),
            "deletedCount",
            deletedCount
        );
    }
}
