package com.eduscope.web.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * APP_USER ↔ APP_ROLE N:M 연결 Entity.
 */
@Entity
@Table(name = "APP_USER_ROLE")
@IdClass(AppUserRoleId.class)
public class AppUserRole {

    @Id
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Id
    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;

    /**
     * USER_ID 기준 사용자 연결.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "USER_ID",
        insertable = false,
        updatable = false
    )
    private AppUser user;

    /**
     * ROLE_ID 기준 권한 연결.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ROLE_ID",
        insertable = false,
        updatable = false
    )
    private AppRole role;

    @Column(
        name = "ASSIGNED_AT",
        nullable = false
    )
    private LocalDateTime assignedAt;

    protected AppUserRole() {
        // JPA 기본 생성자
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public AppUser getUser() {
        return user;
    }

    public AppRole getRole() {
        return role;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}