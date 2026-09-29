package com.eduscope.web.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * EduScope 시스템 로그인 사용자.
 * OULAD_STUDENT와는 별개의 사용자 계정이다.
 */
@Entity

@Table(name = "APP_USER")
public class AppUser {

    @Id
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(
        name = "LOGIN_ID",
        nullable = false,
        length = 100
    )
    private String loginId;

    /**
     * 평문 비밀번호가 아닌 Hash 값.
     */
    @Column(
        name = "PASSWORD_HASH",
        nullable = false,
        length = 255
    )
    private String passwordHash;

    @Column(
        name = "DISPLAY_NAME",
        nullable = false,
        length = 100
    )
    private String displayName;

    /**
     * ACTIVE / LOCKED / DISABLED
     */
    @Column(
        name = "ACCOUNT_STATUS",
        nullable = false,
        length = 20
    )
    private String accountStatus;

    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
        name = "UPDATED_AT",
        nullable = false
    )
    private LocalDateTime updatedAt;

    protected AppUser() {
        // JPA 기본 생성자
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    /**
     * 로그인 성공 시 마지막 로그인 시간을 갱신한다.
     */
    public void updateLastLoginAt(
            LocalDateTime loginAt) {

        this.lastLoginAt = loginAt;
        this.updatedAt = loginAt;
    }
}