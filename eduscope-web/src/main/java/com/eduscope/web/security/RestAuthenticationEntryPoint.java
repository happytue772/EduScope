package com.eduscope.web.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 인증되지 않은 REST 요청을
 * Redirect 대신 JSON 401로 반환한다.
 */
@Component
public class RestAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        response.setStatus(
            HttpServletResponse
                .SC_UNAUTHORIZED
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
                  "message": "UNAUTHORIZED"
                }
                """
            );
    }
}
