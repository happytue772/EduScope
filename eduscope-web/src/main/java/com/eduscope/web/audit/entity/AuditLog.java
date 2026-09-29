package com.eduscope.web.audit.entity;

import java.time.LocalDateTime;

import com.eduscope.web.user.entity.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * EduScope 관리 행위 추적 로그.
 *
 * LOGIN / DATASET_CREATE / JOB_RUN /
 * JOB_RETRY / ROLE_CHANGE 등의 이력을 기록한다.
 */
@Entity
@Table(name = "AUDIT_LOG")
public class AuditLog {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "auditLogSeq"
    )
    @SequenceGenerator(
        name = "auditLogSeq",
        sequenceName = "SEQ_AUDIT_LOG_ID",
        allocationSize = 1
    )
    @Column(name = "AUDIT_ID", nullable = false)
    private Long auditId;

    @Column(name = "USER_ID")
    private Long userId;

    /**
     * USER_ID → APP_USER.USER_ID
     * 읽기 전용 관계.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "USER_ID",
        insertable = false,
        updatable = false
    )
    private AppUser user;

    @Column(
        name = "ACTION_TYPE",
        nullable = false,
        length = 50
    )
    private String actionType;

    @Column(name = "TARGET_TYPE", length = 50)
    private String targetType;

    @Column(name = "TARGET_ID", length = 100)
    private String targetId;

    @Column(name = "REQUEST_URI", length = 1000)
    private String requestUri;

    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    @Column(name = "IP_ADDRESS", length = 64)
    private String ipAddress;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    protected AuditLog() {
        // JPA 기본 생성자
    }

    /**
     * 실제 관리 이벤트 발생 시 Audit Log 생성.
     */
    public AuditLog(
            Long userId,
            String actionType,
            String targetType,
            String targetId,
            String requestUri,
            String description,
            String ipAddress) {

        this.userId = userId;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.requestUri = requestUri;
        this.description = description;
        this.ipAddress = ipAddress;
        this.createdAt = LocalDateTime.now();
    }

    public Long getAuditId() {
        return auditId;
    }

    public Long getUserId() {
        return userId;
    }

    public AppUser getUser() {
        return user;
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