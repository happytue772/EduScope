package com.eduscope.web.audit.dto;

import java.time.LocalDateTime;

import com.eduscope.web.audit.entity.AuditLog;

/**
 * 관리자 Audit Log REST 응답 DTO.
 */
public class AuditLogResponse {

    private final Long auditId;

    private final Long userId;
    private final String loginId;

    private final String actionType;
    private final String targetType;
    private final String targetId;

    private final String requestUri;
    private final String description;
    private final String ipAddress;

    private final LocalDateTime createdAt;

    public AuditLogResponse(
            Long auditId,
            Long userId,
            String loginId,
            String actionType,
            String targetType,
            String targetId,
            String requestUri,
            String description,
            String ipAddress,
            LocalDateTime createdAt) {

        this.auditId = auditId;
        this.userId = userId;
        this.loginId = loginId;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.requestUri = requestUri;
        this.description = description;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    public static AuditLogResponse from(
            AuditLog log) {

        String loginId =
            log.getUser() == null
                ? null
                : log.getUser().getLoginId();

        return new AuditLogResponse(
            log.getAuditId(),
            log.getUserId(),
            loginId,
            log.getActionType(),
            log.getTargetType(),
            log.getTargetId(),
            log.getRequestUri(),
            log.getDescription(),
            log.getIpAddress(),
            log.getCreatedAt()
        );
    }

    public Long getAuditId() {
        return auditId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getDescription() {
        return description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}