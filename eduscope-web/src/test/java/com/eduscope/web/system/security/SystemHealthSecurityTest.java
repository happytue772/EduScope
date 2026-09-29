package com.eduscope.web.system.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import com.eduscope.web.system.service.SystemHealthService;


/**
 * System Health RBAC 자동 테스트.
 *
 * 검증 대상:
 *
 * 미로그인
 *   -> 401
 *
 * VIEWER
 *   -> 403
 *
 * ANALYST
 *   -> 403
 *
 * ADMIN
 *   -> 200
 *
 * 실제 DB 데이터를 생성하거나 수정하지 않는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SystemHealthSecurityTest {


    /**
     * 실제 HTTP 요청을 보내지 않고
     * Spring MVC + Security Filter Chain을
     * 테스트하기 위한 객체.
     */
    @Autowired
    private MockMvc mockMvc;


    /**
     * Security 테스트에서는
     * SystemHealthService 내부 동작이 목적이 아니다.
     *
     * 따라서 실제 Oracle 조회 대신
     * Spring Test의 MockitoBean으로 교체한다.
     */
    @MockitoBean
    private SystemHealthService
        systemHealthService;


    /**
     * 로그인하지 않은 사용자는
     * ADMIN API에 접근할 수 없어야 한다.
     */
    @Test
    void unauthenticatedUserShouldReceive401()
            throws Exception {

        mockMvc.perform(
            get(
                "/api/admin/system-health"
            )
        )
        .andExpect(
            status().isUnauthorized()
        );


        /*
         * Security 단계에서 거부되므로
         * Service까지 호출되면 안 된다.
         */
        verifyNoInteractions(
            systemHealthService
        );
    }


    /**
     * VIEWER는 일반 조회 권한만 가지므로
     * ADMIN System Health에는 접근할 수 없다.
     */
    @Test
    @WithMockUser(
        username = "viewer-test",
        roles = {
            "VIEWER"
        }
    )
    void viewerShouldReceive403()
            throws Exception {

        mockMvc.perform(
            get(
                "/api/admin/system-health"
            )
        )
        .andExpect(
            status().isForbidden()
        );


        verifyNoInteractions(
            systemHealthService
        );
    }


    /**
     * ANALYST도 ADMIN 영역에는
     * 접근할 수 없다.
     */
    @Test
    @WithMockUser(
        username = "analyst-test",
        roles = {
            "ANALYST"
        }
    )
    void analystShouldReceive403()
            throws Exception {

        mockMvc.perform(
            get(
                "/api/admin/system-health"
            )
        )
        .andExpect(
            status().isForbidden()
        );


        verifyNoInteractions(
            systemHealthService
        );
    }


    /**
     * ADMIN은 System Health API에
     * 정상적으로 접근할 수 있어야 한다.
     */
    @Test
    @WithMockUser(
        username = "admin-test",
        roles = {
            "ADMIN"
        }
    )
    void adminShouldReceive200()
            throws Exception {

        /*
         * 이번 테스트의 목적은
         * Health 데이터 내용이 아니라
         * Security 접근 허용 여부다.
         *
         * 실제 DB 조회는
         * SystemHealthIntegrationTest에서
         * 이미 별도로 검증한다.
         */
        when(
            systemHealthService
                .getSystemHealth()
        )
        .thenReturn(
            null
        );


        mockMvc.perform(
            get(
                "/api/admin/system-health"
            )
        )
        .andExpect(
            status().isOk()
        );


        /*
         * ADMIN 요청만 실제 Controller를 통과해서
         * Service에 도달해야 한다.
         */
        verify(
            systemHealthService
        )
        .getSystemHealth();
    }


    /** DEMO_ADMIN은 민감정보를 제외한 상태 화면을 읽을 수 있다. */
    @Test
    @WithMockUser(
        username = "demo-admin-test",
        roles = {
            "DEMO_ADMIN"
        }
    )
    void demoAdminShouldReceive200()
            throws Exception {

        when(
            systemHealthService
                .getSystemHealth()
        )
        .thenReturn(
            null
        );

        mockMvc.perform(
            get(
                "/api/admin/system-health"
            )
        )
        .andExpect(
            status().isOk()
        );

        verify(
            systemHealthService
        )
        .getSystemHealth();
    }
}
