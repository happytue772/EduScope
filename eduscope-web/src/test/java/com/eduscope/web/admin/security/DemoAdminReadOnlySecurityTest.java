package com.eduscope.web.admin.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eduscope.web.admin.dataset.service.AdminDatasetService;
import com.eduscope.web.admin.user.service.AdminUserService;
import com.eduscope.web.audit.service.AuditLogService;

/** DEMO_ADMIN이 관리자 조회만 가능하고 변경은 할 수 없는지 검증한다. */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "demo-admin-test", roles = "DEMO_ADMIN")
class DemoAdminReadOnlySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private AdminDatasetService adminDatasetService;

    @MockitoBean
    private AuditLogService auditLogService;

    @Test
    void demoAdminCanReadAdminUsers() throws Exception {
        when(adminUserService.getUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk());

        verify(adminUserService).getUsers();
    }

    @Test
    void demoAdminCannotChangeUserAccess() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/access")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountStatus\":\"ACTIVE\",\"roles\":[\"ADMIN\"]}"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(adminUserService);
    }

    @Test
    void demoAdminCanReadAdminDatasets() throws Exception {
        when(adminDatasetService.getDatasets()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/datasets"))
            .andExpect(status().isOk());

        verify(adminDatasetService).getDatasets();
    }

    @Test
    void demoAdminCannotCreateDataset() throws Exception {
        mockMvc.perform(post("/api/admin/datasets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(adminDatasetService);
    }

    @Test
    void demoAdminCanReadAuditLogs() throws Exception {
        when(auditLogService.getLogs(0, 20)).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/audit-logs"))
            .andExpect(status().isOk());

        verify(auditLogService).getLogs(0, 20);
    }

    @Test
    void demoAdminCannotPurgeAuditLogs() throws Exception {
        mockMvc.perform(delete("/api/admin/audit-logs/purge")
                .param("before", "2026-01-01")
                .with(csrf()))
            .andExpect(status().isForbidden());

        verifyNoInteractions(auditLogService);
    }

    @Test
    void demoAdminCannotCallFutureMutationApiByDefault() throws Exception {
        mockMvc.perform(post("/api/future-write-endpoint")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }
}
