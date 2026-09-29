package com.eduscope.web.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인 성공 후 처리.
 *
 * 1. APP_USER.LAST_LOGIN_AT 갱신
 * 2. AUDIT_LOG LOGIN 기록
 * 3. 로그인 실패 횟수 초기화
 * 4. React가 화면 이동을 담당하도록 HTTP 200 반환
 */
@Component
public class LoginSuccessHandler
        extends SavedRequestAwareAuthenticationSuccessHandler {

    private final AppUserRepository
        userRepository;

    private final AuditLogService
        auditLogService;

    private final LoginAttemptService
        loginAttemptService;


    public LoginSuccessHandler(
            AppUserRepository userRepository,
            AuditLogService auditLogService,
            LoginAttemptService loginAttemptService) {

        this.userRepository =
            userRepository;

        this.auditLogService =
            auditLogService;

        this.loginAttemptService =
            loginAttemptService;
    }


    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String loginId =
            authentication.getName();


        AppUser user =
            userRepository
                .findByLoginId(
                    loginId
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "로그인 사용자를 DB에서 찾을 수 없습니다."
                    )
                );


        LocalDateTime now =
            LocalDateTime.now();


        /*
         * 마지막 로그인 시간 갱신.
         */
        user.updateLastLoginAt(
            now
        );

        userRepository.save(
            user
        );


        /*
         * 정상 로그인 후
         * 동일 loginId + IP 실패횟수 제거.
         */
        loginAttemptService
            .recordSuccess(
                loginId,
                request.getRemoteAddr()
            );


        /*
         * LOGIN Audit 기록.
         */
        auditLogService.record(
            user.getUserId(),
            "LOGIN",
            "APP_USER",
            String.valueOf(
                user.getUserId()
            ),
            request.getRequestURI(),
            "로그인 성공",
            request.getRemoteAddr()
        );


        /*
         * React SPA에서는 Backend가
         * Redirect하지 않는다.
         */
        response.setStatus(
            HttpServletResponse
                .SC_OK
        );

        response.setCharacterEncoding(
            "UTF-8"
        );

        response.setContentType(
            "application/json;charset=UTF-8"
        );

        response.setHeader(
            "Cache-Control",
            "no-store"
        );

        response
            .getWriter()
            .write(
                """
                {
                  "message": "LOGIN_SUCCESS"
                }
                """
            );
    }
}
