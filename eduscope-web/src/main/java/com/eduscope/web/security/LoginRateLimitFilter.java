package com.eduscope.web.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인 제한 Filter.
 *
 * 이미 제한 상태라면
 * 실제 인증 처리 전에 HTTP 429 반환.
 */
@Component
public class LoginRateLimitFilter
        extends OncePerRequestFilter {

    private static final String LOGIN_URI =
        "/api/auth/login";


    private final LoginAttemptService
        loginAttemptService;


    public LoginRateLimitFilter(
            LoginAttemptService loginAttemptService) {

        this.loginAttemptService =
            loginAttemptService;
    }


    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        return !(
            "POST".equalsIgnoreCase(
                request.getMethod()
            )
            &&
            LOGIN_URI.equals(
                request.getRequestURI()
            )
        );
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String loginId =
            request.getParameter(
                "username"
            );


        String remoteAddr =
            request.getRemoteAddr();


        long remainingSeconds =
            loginAttemptService
                .getRemainingBlockSeconds(
                    loginId,
                    remoteAddr
                );


        /*
         * 로그인 제한 중.
         */
        if (
            remainingSeconds > 0
        ) {

            response.setStatus(
                HttpStatus
                    .TOO_MANY_REQUESTS
                    .value()
            );


            /*
             * HTTP 표준 Retry-After Header.
             */
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


        filterChain.doFilter(
            request,
            response
        );
    }
}