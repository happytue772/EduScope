package com.eduscope.web.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인 실패 처리.
 *
 * - LOGIN_FAILED Audit
 * - 반복 실패 제한 시작
 * - 제한 시작 즉시 HTTP 429
 */
@Component
public class LoginFailureHandler
        implements AuthenticationFailureHandler {

    private final LoginAttemptService
        loginAttemptService;

    private final AppUserRepository
        userRepository;

    private final AuditLogService
        auditLogService;


    public LoginFailureHandler(
            LoginAttemptService loginAttemptService,
            AppUserRepository userRepository,
            AuditLogService auditLogService) {

        this.loginAttemptService =
            loginAttemptService;

        this.userRepository =
            userRepository;

        this.auditLogService =
            auditLogService;
    }


    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String loginId =
            request.getParameter(
                "username"
            );


        String remoteAddr =
            request.getRemoteAddr();


        /*
         * 로그인 실패 횟수 증가.
         */
        boolean blockStarted =
            loginAttemptService
                .recordFailure(
                    loginId,
                    remoteAddr
                );


        Long userId =
            userRepository
                .findByLoginId(
                    loginId == null
                        ? ""
                        : loginId
                )
                .map(
                    AppUser::getUserId
                )
                .orElse(
                    null
                );


        String targetId =
            userId != null
                ? String.valueOf(
                    userId
                )
                : safeTargetId(
                    loginId
                );


        String failureType =
            resolveFailureType(
                exception
            );


        /*
         * 기존 LOGIN_FAILED Audit 유지.
         */
        auditLogService.record(
            userId,
            "LOGIN_FAILED",
            "APP_USER",
            targetId,
            request.getRequestURI(),

            "로그인 실패"
                + " / 유형="
                + failureType
                +
                (
                    blockStarted
                        ? " / 임시 로그인 제한 시작"
                        : ""
                ),

            remoteAddr
        );


        /*
         * 이번 실패로 제한이 시작된 경우
         * 즉시 429 반환.
         */
        if (
            blockStarted
        ) {

            long remainingSeconds =
                loginAttemptService
                    .getRemainingBlockSeconds(
                        loginId,
                        remoteAddr
                    );


            response.setStatus(
                HttpStatus
                    .TOO_MANY_REQUESTS
                    .value()
            );


            response.setHeader(
                "Retry-After",
                String.valueOf(
                    remainingSeconds
                )
            );


            response.setHeader(
                "Cache-Control",
                "no-store"
            );


            response.setCharacterEncoding(
                "UTF-8"
            );


            response.setContentType(
                "application/json;charset=UTF-8"
            );


            response
                .getWriter()
                .write(
                    """
                    {
                      "message": "LOGIN_RATE_LIMIT",
                      "retryAfterSeconds": %d
                    }
                    """
                    .formatted(
                        remainingSeconds
                    )
                );


            return;
        }


        /*
         * 일반 로그인 실패.
         */
        response.setStatus(
            HttpServletResponse
                .SC_UNAUTHORIZED
        );


        response.setHeader(
            "Cache-Control",
            "no-store"
        );


        response.setCharacterEncoding(
            "UTF-8"
        );


        response.setContentType(
            "application/json;charset=UTF-8"
        );


        response
            .getWriter()
            .write(
                """
                {
                  "message": "LOGIN_FAILED"
                }
                """
            );
    }


    private String resolveFailureType(
            AuthenticationException exception) {

        if (
            exception
            instanceof LockedException
        ) {

            return "LOCKED";
        }


        if (
            exception
            instanceof DisabledException
        ) {

            return "DISABLED";
        }


        if (
            exception
            instanceof BadCredentialsException
        ) {

            return "BAD_CREDENTIALS";
        }


        return "AUTHENTICATION_FAILURE";
    }


    private String safeTargetId(
            String loginId) {

        if (
            loginId == null
            ||
            loginId.isBlank()
        ) {

            return "UNKNOWN";
        }


        String value =
            loginId.trim();


        return value.length() <= 100
            ? value
            : value.substring(
                0,
                100
            );
    }
}