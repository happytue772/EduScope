package com.eduscope.web.analysis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduscope.web.analysis.dto.AnalysisJobExecutionResponse;
import com.eduscope.web.analysis.entity.AnalysisJob;
import com.eduscope.web.analysis.execution.AnalysisExecutionPlan;
import com.eduscope.web.analysis.execution.AnalysisExecutionRegistry;
import com.eduscope.web.analysis.repository.AnalysisJobRepository;
import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.dataset.entity.Dataset;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;

/** 실행 시작의 상태 전환만 검사하며 SSH/Hadoop Worker는 호출 기록만 확인한다. */
@ExtendWith(MockitoExtension.class)
class AnalysisJobExecutionServiceTest {

    @Mock private AnalysisJobRepository repository;
    @Mock private AnalysisExecutionRegistry registry;
    @Mock private AnalysisJobExecutionWorker worker;
    @Mock private AppUserRepository appUserRepository;
    @Mock private AuditLogService auditLogService;
    @InjectMocks private AnalysisJobExecutionService service;

    @Test
    void pendingJobStartsRunningAndDispatchesWorker() {
        AnalysisJob job = AnalysisJob.createPending(
            org.mockito.Mockito.mock(Dataset.class), 7L,
            "ASSESSMENT", "v1", "/input", "/output/new"
        );
        job.setJobId(42L);
        AnalysisExecutionPlan plan = org.mockito.Mockito.mock(AnalysisExecutionPlan.class);
        AppUser actor = org.mockito.Mockito.mock(AppUser.class);
        when(repository.findById(42L)).thenReturn(Optional.of(job));
        when(registry.resolve(job)).thenReturn(plan);
        when(appUserRepository.findByLoginId("admin")).thenReturn(Optional.of(actor));
        when(actor.getUserId()).thenReturn(7L);

        AnalysisJobExecutionResponse response = service.startExecution(
            42L, "admin", "/api/analysis-jobs/42/execute", "127.0.0.1"
        );

        assertEquals("RUNNING", job.getStatus());
        assertEquals("RUNNING", response.getStatus());
        assertNotNull(response.getStartedAt());
        verify(repository).saveAndFlush(job);
        verify(auditLogService).record(
            eq(7L), eq("JOB_RUN"), eq("ANALYSIS_JOB"), eq("42"),
            eq("/api/analysis-jobs/42/execute"), anyString(), eq("127.0.0.1")
        );
        verify(worker).executeAsync(42L, plan);
    }

    @Test
    void nonPendingJobNeverStartsWorker() {
        AnalysisJob job = AnalysisJob.createPending(
            org.mockito.Mockito.mock(Dataset.class), 7L,
            "ASSESSMENT", "v1", "/input", "/output/old"
        );
        job.setJobId(42L);
        job.markFailed("HADOOP", "실행 실패", 10L);
        when(repository.findById(42L)).thenReturn(Optional.of(job));

        assertThrows(IllegalStateException.class, () ->
            service.startExecution(42L, "admin", "/api/analysis-jobs/42/execute", "127.0.0.1")
        );

        assertEquals("FAILED", job.getStatus());
        verify(repository, never()).saveAndFlush(job);
        verifyNoInteractions(registry, worker, auditLogService);
    }
}
