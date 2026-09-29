package com.eduscope.web.analysis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduscope.web.analysis.dto.AnalysisJobCommandResponse;
import com.eduscope.web.analysis.dto.AnalysisJobCreateRequest;
import com.eduscope.web.analysis.dto.AnalysisJobRetryRequest;
import com.eduscope.web.analysis.entity.AnalysisJob;
import com.eduscope.web.analysis.repository.AnalysisJobRepository;
import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.dataset.entity.Dataset;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;

import jakarta.persistence.EntityManager;

/** Repository/Oracle에 실제로 쓰지 않고 Job 요청의 핵심 규칙을 검증한다. */
@ExtendWith(MockitoExtension.class)
class AnalysisJobServiceTest {

    @Mock private AnalysisJobRepository repository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private EntityManager entityManager;
    @InjectMocks private AnalysisJobService service;

    @Test
    void createJobRegistersPendingJobAndRequestAudit() {
        Dataset dataset = org.mockito.Mockito.mock(Dataset.class);
        AppUser user = org.mockito.Mockito.mock(AppUser.class);
        when(repository.countActiveDataset(1L)).thenReturn(1);
        when(repository.existsByHdfsOutputPath("/output/new")).thenReturn(false);
        when(entityManager.find(Dataset.class, 1L)).thenReturn(dataset);
        when(dataset.getDatasetId()).thenReturn(1L);
        when(appUserRepository.findByLoginId("admin")).thenReturn(Optional.of(user));
        when(user.getUserId()).thenReturn(7L);
        when(repository.save(any(AnalysisJob.class))).thenAnswer(invocation -> {
            AnalysisJob job = invocation.getArgument(0);
            job.setJobId(42L); // 영속화 결과의 ID만 모의한다.
            return job;
        });

        AnalysisJobCommandResponse response = service.createJob(
            new AnalysisJobCreateRequest(1L, " assessment ", " v1 ", " /input ", " /output/new "),
            "admin", "/api/analysis-jobs", "127.0.0.1"
        );

        ArgumentCaptor<AnalysisJob> saved = ArgumentCaptor.forClass(AnalysisJob.class);
        verify(repository).save(saved.capture());
        assertEquals("PENDING", saved.getValue().getStatus());
        assertEquals("ASSESSMENT", response.analysisType());
        assertEquals("v1", response.analysisVersion());
        assertEquals("/input", response.hdfsInputPath());
        assertEquals("/output/new", response.hdfsOutputPath());
        assertEquals(42L, response.jobId());
        verify(auditLogService).record(
            eq(7L), eq("JOB_REQUEST"), eq("ANALYSIS_JOB"), eq("42"),
            eq("/api/analysis-jobs"), anyString(), eq("127.0.0.1")
        );
    }

    @Test
    void createJobRejectsDuplicateOutputBeforeSaving() {
        when(repository.countActiveDataset(1L)).thenReturn(1);
        when(repository.existsByHdfsOutputPath("/output/used")).thenReturn(true);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
            service.createJob(
                new AnalysisJobCreateRequest(1L, "ASSESSMENT", "v1", "/input", "/output/used"),
                "admin", "/api/analysis-jobs", "127.0.0.1"
            )
        );

        assertTrue(error.getMessage().contains("이미 사용된 HDFS Output 경로"));
        verify(repository, never()).save(any(AnalysisJob.class));
        verify(auditLogService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void retryRejectsNonFailedJobWithoutSaving() {
        AnalysisJob pending = AnalysisJob.createPending(
            org.mockito.Mockito.mock(Dataset.class), 7L, "ASSESSMENT", "v1", "/input", "/output/old"
        );
        when(repository.findById(42L)).thenReturn(Optional.of(pending));

        assertThrows(IllegalStateException.class, () ->
            service.retryJob(42L, new AnalysisJobRetryRequest("/output/new"),
                "admin", "/api/analysis-jobs/42/retry", "127.0.0.1")
        );

        assertEquals("PENDING", pending.getStatus());
        verify(repository, never()).save(any(AnalysisJob.class));
    }

    @Test
    void retryFailedJobCreatesNewPendingJobWithoutChangingOriginal() {
        Dataset dataset = org.mockito.Mockito.mock(Dataset.class);
        AppUser user = org.mockito.Mockito.mock(AppUser.class);
        AnalysisJob failed = AnalysisJob.createPending(dataset, 7L, "ASSESSMENT", "v1", "/input", "/output/old");
        failed.setJobId(42L);
        failed.markFailed("HADOOP", "실행 실패", 10L);
        when(dataset.getDatasetId()).thenReturn(1L);
        when(repository.findById(42L)).thenReturn(Optional.of(failed));
        when(repository.countActiveDataset(1L)).thenReturn(1);
        when(repository.existsByHdfsOutputPath("/output/new")).thenReturn(false);
        when(appUserRepository.findByLoginId("admin")).thenReturn(Optional.of(user));
        when(user.getUserId()).thenReturn(7L);
        when(repository.save(any(AnalysisJob.class))).thenAnswer(invocation -> {
            AnalysisJob job = invocation.getArgument(0);
            job.setJobId(43L);
            return job;
        });

        AnalysisJobCommandResponse response = service.retryJob(
            42L, new AnalysisJobRetryRequest(" /output/new "),
            "admin", "/api/analysis-jobs/42/retry", "127.0.0.1"
        );

        ArgumentCaptor<AnalysisJob> saved = ArgumentCaptor.forClass(AnalysisJob.class);
        verify(repository).save(saved.capture());
        assertNotSame(failed, saved.getValue());
        assertEquals("FAILED", failed.getStatus());
        assertEquals("/output/old", failed.getHdfsOutputPath());
        assertEquals("PENDING", response.status());
        assertEquals(42L, response.previousJobId());
        assertEquals(43L, response.jobId());
        assertEquals("/output/new", response.hdfsOutputPath());
        verify(auditLogService).record(
            eq(7L), eq("JOB_RETRY"), eq("ANALYSIS_JOB"), eq("42"),
            eq("/api/analysis-jobs/42/retry"), anyString(), eq("127.0.0.1")
        );
    }
}
