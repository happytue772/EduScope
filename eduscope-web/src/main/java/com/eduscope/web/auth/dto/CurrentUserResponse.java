package com.eduscope.web.auth.dto;

import java.util.List;

/**
 * 현재 로그인한 EduScope 사용자 정보 응답 DTO.
 */
public class CurrentUserResponse {

    private final Long userId;
    private final String loginId;
    private final String displayName;
    private final String accountStatus;
    private final List<String> roles;

    public CurrentUserResponse(
            Long userId,
            String loginId,
            String displayName,
            String accountStatus,
            List<String> roles) {

        this.userId = userId;
        this.loginId = loginId;
        this.displayName = displayName;
        this.accountStatus = accountStatus;
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

    public List<String> getRoles() {
        return roles;
    }
}