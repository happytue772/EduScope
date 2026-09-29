package com.eduscope.web.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * EduScope RBAC 권한.
 */
@Entity
@Table(name = "APP_ROLE")
public class AppRole {

    @Id
    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;

    /**
     * VIEWER / ANALYST / DEMO_ADMIN / ADMIN
     */
    @Column(
        name = "ROLE_CODE",
        nullable = false,
        length = 30
    )
    private String roleCode;

    @Column(
        name = "ROLE_NAME",
        nullable = false,
        length = 100
    )
    private String roleName;

    @Column(
        name = "DESCRIPTION",
        length = 500
    )
    private String description;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    protected AppRole() {
        // JPA 기본 생성자
    }

    public Long getRoleId() {
        return roleId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
