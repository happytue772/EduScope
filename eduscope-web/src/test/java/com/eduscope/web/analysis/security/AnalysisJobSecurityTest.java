package com.eduscope.web.analysis.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eduscope.web.analysis.dto.AnalysisJobCreateRequest;
import com.eduscope.web.analysis.dto.AnalysisJobRetryRequest;
import com.eduscope.web.analysis.service.AnalysisJobExecutionService;
import com.eduscope.web.analysis.service.AnalysisJobService;

/** 실제 Security Filter Chain으로 Job 조회/변경 권한을 검사한다. */
@SpringBootTest
@AutoConfigureMockMvc
class AnalysisJobSecurityTest {

    private static final String PATH = "/api/analysis-jobs";
    private static final String CREATE_BODY = """
        {"datasetId":1,"analysisType":"ASSESSMENT","analysisVersion":"v1",
         "hdfsInputPath":"/input","hdfsOutputPath":"/output/new"}
        """;

    @Autowired private MockMvc mockMvc;
    // 컨트롤러까지 통과하는지만 검사한다. DB 저장과 Hadoop 실행은 일어나지 않는다.
    @MockitoBean private AnalysisJobService service;
    @MockitoBean private AnalysisJobExecutionService executionService;

    @Test
    void unauthenticatedCannotReadJobs() throws Exception {
        mockMvc.perform(get(PATH)).andExpect(status().isUnauthorized());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerCannotReadJobs() throws Exception {
        mockMvc.perform(get(PATH)).andExpect(status().isForbidden());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void analystCanReadJobs() throws Exception {
        when(service.getJobs()).thenReturn(List.of());
        mockMvc.perform(get(PATH)).andExpect(status().isOk());
        verify(service).getJobs();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadJobs() throws Exception {
        when(service.getJobs()).thenReturn(List.of());
        mockMvc.perform(get(PATH)).andExpect(status().isOk());
        verify(service).getJobs();
    }

    @Test
    @WithMockUser(roles = "DEMO_ADMIN")
    void demoAdminCanReadJobs() throws Exception {
        when(service.getJobs()).thenReturn(List.of());
        mockMvc.perform(get(PATH)).andExpect(status().isOk());
        verify(service).getJobs();
    }

    @Test
    void unauthenticatedCannotCreateJob() throws Exception {
        mockMvc.perform(post(PATH).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_BODY)).andExpect(status().isUnauthorized());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerCannotCreateJob() throws Exception {
        mockMvc.perform(post(PATH).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_BODY)).andExpect(status().isForbidden());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void analystCannotCreateJob() throws Exception {
        mockMvc.perform(post(PATH).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_BODY)).andExpect(status().isForbidden());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(roles = "DEMO_ADMIN")
    void demoAdminCannotCreateJob() throws Exception {
        mockMvc.perform(post(PATH).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_BODY)).andExpect(status().isForbidden());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanCreateJob() throws Exception {
        mockMvc.perform(post(PATH).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_BODY)).andExpect(status().isCreated());
        verify(service).createJob(any(AnalysisJobCreateRequest.class), eq("admin"), eq(PATH), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPostWithoutCsrfIsForbidden() throws Exception {
        mockMvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_BODY)).andExpect(status().isForbidden());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void analystCannotRetryFailedJob() throws Exception {
        mockMvc.perform(post(PATH + "/42/retry").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"hdfsOutputPath\":\"/output/new\"}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(roles = "DEMO_ADMIN")
    void demoAdminCannotRetryFailedJob() throws Exception {
        mockMvc.perform(post(PATH + "/42/retry").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"hdfsOutputPath\":\"/output/new\"}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanRetryFailedJob() throws Exception {
        mockMvc.perform(post(PATH + "/42/retry").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"hdfsOutputPath\":\"/output/new\"}"))
            .andExpect(status().isCreated());
        verify(service).retryJob(eq(42L), any(AnalysisJobRetryRequest.class),
            eq("admin"), eq(PATH + "/42/retry"), anyString());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void analystCannotExecuteJob() throws Exception {
        mockMvc.perform(post(PATH + "/42/execute").with(csrf()))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(roles = "DEMO_ADMIN")
    void demoAdminCannotExecuteJob() throws Exception {
        mockMvc.perform(post(PATH + "/42/execute").with(csrf()))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service, executionService);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanExecuteJob() throws Exception {
        mockMvc.perform(post(PATH + "/42/execute").with(csrf()))
            .andExpect(status().isAccepted());
        verify(executionService).startExecution(eq(42L), eq("admin"),
            eq(PATH + "/42/execute"), anyString());
    }
}
