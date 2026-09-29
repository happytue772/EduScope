package com.eduscope.web.admin.user.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 사용자 계정 상태 및 Role 변경 요청.
 *
 * ACTIVE:
 * - 최소 하나의 Role 필요
 *
 * LOCKED / DISABLED:
 * - Role이 없어도 가능
 */
public record UserAccessUpdateRequest(

    @NotBlank
    String accountStatus,

    @NotNull
    List<String> roles

) {
}