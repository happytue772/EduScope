package com.eduscope.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import jakarta.servlet.http.HttpServletResponse;

/**
 * EduScope 인증 / 인가 / Session 보안 설정.
 *
 * VIEWER
 * - 일반 조회
 *
 * ANALYST
 * - VIEWER 영역
 * - 학생 상세
 * - Dataset / Data Quality / Job 조회
 *
 * DEMO_ADMIN
 * - ANALYST 조회 영역
 * - 관리자 화면 읽기 전용 조회
 * - 데이터 변경 / Job 실행 불가
 *
 * ADMIN
 * - 사용자 / Role / Audit / Actuator
 * - Dataset 관리
 * - Analysis Job 생성 / 실행 / 재실행
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * APP_USER.PASSWORD_HASH는 BCrypt 사용.
     */
    @Bean
    PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    /**
     * 로그인 Session 추적용 Registry.
     *
     * 계정 상태 또는 Role 변경 시
     * 기존 Session을 만료시키는 데 사용한다.
     */
    @Bean
    SessionRegistry sessionRegistry() {

        return new SessionRegistryImpl();
    }


    /**
     * Session 생성 / 제거 이벤트를
     * SessionRegistry에 전달한다.
     */
    @Bean
    HttpSessionEventPublisher
        httpSessionEventPublisher() {

        return new HttpSessionEventPublisher();
    }


    /**
     * EduScope Security Filter Chain.
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            LoginRateLimitFilter loginRateLimitFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            SessionRegistry sessionRegistry)
            throws Exception {

        http.authorizeHttpRequests(
            auth -> auth

                /* =============================================
                 * 인증 없이 접근 가능한 API
                 * ============================================= */
                .requestMatchers(
                    "/error",
                    "/actuator/health",
                    "/api/auth/login",
                    "/api/auth/signup",
                    "/api/auth/csrf"
                )
                .permitAll()


                /* =============================================
                 * 관리자 화면 조회
                 *
                 * DEMO_ADMIN은 공개 포트폴리오에서
                 * 관리자 화면을 읽기 전용으로 확인한다.
                 * 반드시 상태 변경 Catch-all 규칙보다 앞에 둔다.
                 * ============================================= */
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/admin/**"
                )
                .hasAnyRole(
                    "DEMO_ADMIN",
                    "ADMIN"
                )


                /* =============================================
                 * 관리자 상태 변경 및 Actuator는 ADMIN 전용
                 * ============================================= */
                .requestMatchers(
                    "/api/admin/**",
                    "/actuator/**"
                )
                .hasRole(
                    "ADMIN"
                )


                /* =============================================
                 * Dataset 상태 변경
                 * 요구사항상 ADMIN만 가능.
                 * ============================================= */
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/datasets/**",
                    "/api/dataset-files/**",
                    "/api/dataset-details/**"
                )
                .hasRole(
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/datasets/**",
                    "/api/dataset-files/**",
                    "/api/dataset-details/**"
                )
                .hasRole(
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/datasets/**",
                    "/api/dataset-files/**",
                    "/api/dataset-details/**"
                )
                .hasRole(
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/datasets/**",
                    "/api/dataset-files/**",
                    "/api/dataset-details/**"
                )
                .hasRole(
                    "ADMIN"
                )


                /* =============================================
                 * Analysis Job 상태 변경
                 * 생성 / 실행 / 재실행은 ADMIN만 가능.
                 * ============================================= */
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/analysis-jobs/**"
                )
                .hasRole(
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/analysis-jobs/**"
                )
                .hasRole(
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/analysis-jobs/**"
                )
                .hasRole(
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/analysis-jobs/**"
                )
                .hasRole(
                    "ADMIN"
                )


                /* =============================================
                 * API 상태 변경 기본 차단
                 *
                 * 앞으로 새 API가 추가되어도 별도 허용 규칙이
                 * 없으면 ADMIN만 변경할 수 있게 한다.
                 * 로그인 / 회원가입은 위 permitAll 규칙이 먼저 적용된다.
                 * ============================================= */
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/**"
                )
                .hasRole(
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/**"
                )
                .hasRole(
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/**"
                )
                .hasRole(
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/**"
                )
                .hasRole(
                    "ADMIN"
                )


                /* =============================================
                 * ANALYST / ADMIN 전용 조회
                 * ============================================= */
                .requestMatchers(
                    HttpMethod.GET,

                    "/api/students/**",
                    "/api/student-analysis/**",
                    "/api/student-courses/**",
                    "/api/student-registrations/**",
                    "/api/student-assessments/**",

                    "/api/analysis/student-activity/**",
                    "/api/analysis/student-learning-summary/**",

                    "/api/analysis/data-quality/**",

                    "/api/datasets/**",
                    "/api/dataset-files/**",
                    "/api/dataset-details/**",

                    "/api/analysis-jobs/**",
                    "/api/analysis-job-overview/**"
                )
                .hasAnyRole(
                    "ANALYST",
                    "DEMO_ADMIN",
                    "ADMIN"
                )


                /* =============================================
                 * 일반 조회
                 * VIEWER / ANALYST / ADMIN
                 * ============================================= */
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/**"
                )
                .hasAnyRole(
                    "VIEWER",
                    "ANALYST",
                    "DEMO_ADMIN",
                    "ADMIN"
                )


                /*
                 * 위에서 별도 지정하지 않은 요청도
                 * 최소 로그인 필요.
                 */
                .anyRequest()
                .authenticated()
        );


        /* =============================================
         * REST용 401 / 403 JSON
         * ============================================= */
        http.exceptionHandling(
            exception -> exception
                .authenticationEntryPoint(
                    authenticationEntryPoint
                )
                .accessDeniedHandler(
                    accessDeniedHandler
                )
        );


        /* =============================================
         * Session 보안
         *
         * - 로그인 성공 시 Session ID 교체
         * - SessionRegistry로 로그인 Session 추적
         * - 최대 Session 수 제한은 현재 두지 않음
         * ============================================= */
        http.sessionManagement(
            session -> {

                session.sessionFixation(
                    fixation ->
                        fixation.changeSessionId()
                );


                session
                    .maximumSessions(
                        -1
                    )
                    .sessionRegistry(
                        sessionRegistry
                    );
            }
        );


        /* =============================================
         * 로그인
         * ============================================= */
        http.formLogin(
            form -> form

                /*
                 * React /login과 Backend 인증 URL 분리.
                 */
                .loginProcessingUrl(
                    "/api/auth/login"
                )

                .successHandler(
                    loginSuccessHandler
                )

                .failureHandler(
                    loginFailureHandler
                )

                .permitAll()
        );


        /*
         * 실제 비밀번호 인증 전에
         * 반복 로그인 제한 검사.
         */
        http.addFilterBefore(
            loginRateLimitFilter,
            UsernamePasswordAuthenticationFilter.class
        );


        /* =============================================
         * Logout
         *
         * CSRF가 활성화되어 있으므로
         * React에서는 CSRF Token과 함께
         * POST /api/auth/logout 요청을 사용한다.
         * ============================================= */
        http.logout(
            logout -> logout

                .logoutUrl(
                    "/api/auth/logout"
                )

                .invalidateHttpSession(
                    true
                )

                .clearAuthentication(
                    true
                )

                .deleteCookies(
                    "JSESSIONID"
                )

                .logoutSuccessHandler(
                    (
                        request,
                        response,
                        authentication
                    ) -> {

                        response.setStatus(
                            HttpServletResponse
                                .SC_NO_CONTENT
                        );
                    }
                )
        );


        /*
         * CSRF는 비활성화하지 않는다.
         * Spring Security 기본 CSRF 보호를 유지한다.
         */


        return http.build();
    }
}
