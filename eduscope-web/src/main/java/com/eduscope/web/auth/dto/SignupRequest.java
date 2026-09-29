package com.eduscope.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO.
 *
 * Role은 사용자가 직접 선택하지 않는다.
 * 가입 후 관리자가 별도로 부여한다.
 */
public record SignupRequest(

    @NotBlank
    @Size(min = 4, max = 100)
    String loginId,

    @NotBlank
    @Size( max = 100)
    String password,

    @NotBlank
    @Size( max = 100)
    String passwordConfirm,

    @NotBlank
    @Size(max = 100)
    String displayName

) {
}