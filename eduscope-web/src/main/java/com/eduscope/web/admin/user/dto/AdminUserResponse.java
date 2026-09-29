package com.eduscope.web.admin.user.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 사용자 조회 응답 DTO.
 *
 * 비밀번호 Hash는 외부로 반환하지 않는다.
 */
public class AdminUserResponse {

    private final Long userId;
    private final String loginId;
    private final String displayName;
    private final String accountStatus;

    private final LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private final List<String> roles;

    public AdminUserResponse(
            Long userId,
            String loginId,
            String displayName,
            String accountStatus,
            LocalDateTime lastLoginAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<String> roles) {

        this.userId = userId;
        this.loginId = loginId;
        this.displayName = displayName;
        this.accountStatus = accountStatus;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginId() {
        return loginId;
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

    public List<String> getRoles() {
        return roles;
    }
}