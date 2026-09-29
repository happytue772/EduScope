package com.eduscope.web.analysis.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.AnalysisJobExecutionResponse;
import com.eduscope.web.analysis.entity.AnalysisJob;
import com.eduscope.web.analysis.execution.AnalysisExecutionPlan;
import com.eduscope.web.analysis.execution.AnalysisExecutionRegistry;
import com.eduscope.web.analysis.repository.AnalysisJobRepository;
import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;

/**
 * 실제 Analysis Job 실행 시작 Service.
 */
@Service
public class AnalysisJobExecutionService {

    private final AnalysisJobRepository repository;

    private final AnalysisExecutionRegistry registry;

    private final AnalysisJobExecutionWorker worker;

    private final AppUserRepository appUserRepository;

    private final AuditLogService auditLogService;


    public AnalysisJobExecutionService(
            AnalysisJobRepository repository,
            AnalysisExecutionRegistry registry,
            AnalysisJobExecutionWorker worker,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService) {

        this.repository =
            repository;

        this.registry =
            registry;

        this.worker =
            worker;

        this.appUserRepository =
            appUserRepository;

        this.auditLogService =
            auditLogService;
    }


    /**
     * PENDING Job을 실제 Hadoop 실행으로 넘긴다.
     */
    @Transactional
    public AnalysisJobExecutionResponse startExecution(
            Long jobId,
            String loginId,
            String requestUri,
            String ipAddress) {

        AnalysisJob job =
            repository
                .findById(
                    jobId
                )
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "ANALYSIS_JOB을 찾을 수 없습니다. jobId="
                        + jobId
                    )
                );


        if (
            !"PENDING".equals(
                job.getStatus()
            )
        ) {

            throw new IllegalStateException(
                "PENDING 상태의 Job만 실행할 수 있습니다."
            );
        }


        /*
         * RUNNING으로 변경하기 전에
         * Driver/경로 구성이 실제 실행 가능한지 검증한다.
         */
        AnalysisExecutionPlan plan =
            registry.resolve(
                job
            );


        AppUser actor =
            appUserRepository
                .findByLoginId(
                    loginId
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "현재 로그인 사용자를 찾을 수 없습니다."
                    )
                );


        job.markRunning();


        repository.saveAndFlush(
            job
        );


        /*
         * 실제 Hadoop 실행이 시작되는 시점에만
         * JOB_RUN Audit을 남긴다.
         */
        auditLogService.record(
            actor.getUserId(),
            "JOB_RUN",
            "ANALYSIS_JOB",
            String.valueOf(
                job.getJobId()
            ),
            requestUri,
            "실제 MapReduce 실행 시작: "
                + job.getAnalysisType()
                + ", Version="
                + job.getAnalysisVersion(),
            ipAddress
        );


        /*
         * SSH/Hadoop 실행은 별도 Thread에서 수행한다.
         */
        worker.executeAsync(
            job.getJobId(),
            plan
        );


        return new AnalysisJobExecutionResponse(
            job.getJobId(),
            job.getStatus(),
            job.getStartedAt()
        );
    }
}