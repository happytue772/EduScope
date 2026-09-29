package com.eduscope.web.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 인증은 되었지만 권한이 없는 REST 요청을
 * JSON 403으로 반환한다.
 *
 * CSRF 실패도 Spring Security 영역에서
 * 동일하게 403 처리될 수 있다.
 */
@Component
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setStatus(
            HttpServletResponse
                .SC_FORBIDDEN
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
                  "message": "FORBIDDEN"
                }
                """
            );
    }
}
