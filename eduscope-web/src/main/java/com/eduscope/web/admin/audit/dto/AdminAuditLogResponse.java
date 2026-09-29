package com.eduscope.web.admin.audit.dto;

import java.time.LocalDateTime;

/**
 * 관리자 Audit Log 조회 DTO.
 */
public record AdminAuditLogResponse(

    Long auditId,

    Long userId,

    String loginId,

    String displayName,

    String actionType,

    String targetType,

    String targetId,

    String requestUri,

    String description,

    String ipAddress,

    LocalDateTime createdAt

) {
}