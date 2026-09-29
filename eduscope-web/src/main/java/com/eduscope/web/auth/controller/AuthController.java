package com.eduscope.web.auth.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.auth.dto.CurrentUserResponse;
import com.eduscope.web.auth.dto.SignupRequest;
import com.eduscope.web.auth.service.SignupService;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;
import com.eduscope.web.user.repository.AppUserRoleRepository;

import jakarta.validation.Valid;

/**
 * EduScope 인증 관련 API.
 *
 * 기능:
 * 1. 현재 로그인 사용자 조회
 * 2. 회원가입
 * 3. CSRF Token 조회
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository
        userRepository;

    private final AppUserRoleRepository
        userRoleRepository;

    private final SignupService
        signupService;


    public AuthController(
            AppUserRepository userRepository,
            AppUserRoleRepository userRoleRepository,
            SignupService signupService) {

        this.userRepository =
            userRepository;

        this.userRoleRepository =
            userRoleRepository;

        this.signupService =
            signupService;
    }


    /**
     * 현재 로그인 사용자와
     * DB의 실제 Role을 반환한다.
     */
    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(
            Principal principal) {

        AppUser user =
            userRepository
                .findByLoginId(
                    principal.getName()
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "현재 로그인 사용자를 찾을 수 없습니다."
                    )
                );


        List<String> roles =
            userRoleRepository
                .findAllWithRoleByUserId(
                    user.getUserId()
                )
                .stream()
                .map(userRole ->
                    userRole
                        .getRole()
                        .getRoleCode()
                )
                .toList();


        return new CurrentUserResponse(
            user.getUserId(),
            user.getLoginId(),
            user.getDisplayName(),
            user.getAccountStatus(),
            roles
        );
    }


    /**
     * 회원가입.
     *
     * 기존 정책:
     * ACCOUNT_STATUS = DISABLED
     * Role = 없음
     *
     * ADMIN 승인 후 ACTIVE + Role로 전환한다.
     */
    @PostMapping("/signup")
    public Map<String, Object> signup(
            @Valid
            @RequestBody
            SignupRequest request) {

        Long userId =
            signupService.signup(
                request
            );


        return Map.of(
            "userId",
            userId,
            "message",
            "회원가입이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다."
        );
    }


    /**
     * React에서 POST / PATCH / DELETE 요청 시
     * 사용할 CSRF Token.
     */
    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken(
            CsrfToken csrfToken) {

        return Map.of(
            "token",
            csrfToken.getToken(),
            "headerName",
            csrfToken.getHeaderName(),
            "parameterName",
            csrfToken.getParameterName()
        );
    }
}
