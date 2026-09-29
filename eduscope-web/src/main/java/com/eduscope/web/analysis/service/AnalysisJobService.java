package com.eduscope.web.analysis.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.AnalysisJobCommandResponse;
import com.eduscope.web.analysis.dto.AnalysisJobCreateRequest;
import com.eduscope.web.analysis.dto.AnalysisJobResponse;
import com.eduscope.web.analysis.dto.AnalysisJobRetryRequest;
import com.eduscope.web.analysis.entity.AnalysisJob;
import com.eduscope.web.analysis.repository.AnalysisJobRepository;
import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.dataset.entity.Dataset;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;

import jakarta.persistence.EntityManager;

/**
 * Analysis Job 조회 / 요청 Service.
 */
@Service
@Transactional(readOnly = true)
public class AnalysisJobService {

    /**
     * EduScope에서 실제 지원하는
     * 10개 분석 유형만 허용한다.
     */
    private static final Set<String>
        SUPPORTED_ANALYSIS_TYPES =
            Set.of(

                "STUDENT_ACTIVITY",

                "COURSE_ACTIVITY",

                "COURSE_WEEKLY_ACTIVITY",

                "VLE_ACTIVITY",

                "ASSESSMENT",

                "REGISTRATION",

                "COURSE_RESULT",

                "ACTIVITY_RESULT",

                "STUDENT_LEARNING_SUMMARY",

                "DATA_QUALITY"
            );


    private final AnalysisJobRepository repository;

    private final AppUserRepository appUserRepository;

    private final AuditLogService auditLogService;

    private final EntityManager entityManager;


    public AnalysisJobService(
            AnalysisJobRepository repository,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService,
            EntityManager entityManager) {

        this.repository =
            repository;

        this.appUserRepository =
            appUserRepository;

        this.auditLogService =
            auditLogService;

        this.entityManager =
            entityManager;
    }


    /**
     * 기존 전체 조회.
     */
    public List<AnalysisJobResponse> getJobs() {

        return repository
            .findAllByOrderByJobIdDesc()
            .stream()
            .map(
                AnalysisJobResponse::from
            )
            .toList();
    }


    /**
     * 기존 단건 조회.
     */
    public AnalysisJobResponse getJob(
            Long jobId) {

        AnalysisJob job =
            getJobEntity(
                jobId
            );


        return AnalysisJobResponse.from(
            job
        );
    }


    /**
     * 기존 분석 유형별 조회.
     */
    public List<AnalysisJobResponse>
            getJobsByType(
                String analysisType) {

        String normalizedType =
            normalizeAnalysisType(
                analysisType
            );


        return repository
            .findByAnalysisTypeOrderByJobIdDesc(
                normalizedType
            )
            .stream()
            .map(
                AnalysisJobResponse::from
            )
            .toList();
    }


    /**
     * 신규 Analysis Job 요청.
     *
     * 실제 MapReduce 실행은 하지 않는다.
     * PENDING 상태만 Oracle에 등록한다.
     */
    @Transactional
    public AnalysisJobCommandResponse createJob(
            AnalysisJobCreateRequest request,
            String loginId,
            String requestUri,
            String ipAddress) {

        Long datasetId =
            request.datasetId();


        validateActiveDataset(
            datasetId
        );


        String analysisType =
            normalizeAnalysisType(
                request.analysisType()
            );


        String analysisVersion =
            normalizeRequired(
                request.analysisVersion(),
                "analysisVersion"
            );


        String hdfsInputPath =
            normalizeRequired(
                request.hdfsInputPath(),
                "hdfsInputPath"
            );


        String hdfsOutputPath =
            normalizeRequired(
                request.hdfsOutputPath(),
                "hdfsOutputPath"
            );


        validateOutputPathNotUsed(
            hdfsOutputPath
        );


        Dataset dataset =
            entityManager.find(
                Dataset.class,
                datasetId
            );


        if (dataset == null) {

            throw new IllegalArgumentException(
                "Dataset을 찾을 수 없습니다. datasetId="
                + datasetId
            );
        }


        AppUser requester =
            getRequester(
                loginId
            );


        AnalysisJob job =
            AnalysisJob.createPending(

                dataset,

                requester.getUserId(),

                analysisType,

                analysisVersion,

                hdfsInputPath,

                hdfsOutputPath
            );


        AnalysisJob saved =
            repository.save(
                job
            );


        /*
         * 아직 실제 Hadoop 실행 전이므로
         * JOB_RUN이 아니라 JOB_REQUEST로 기록한다.
         */
        auditLogService.record(

            requester.getUserId(),

            "JOB_REQUEST",

            "ANALYSIS_JOB",

            String.valueOf(
                saved.getJobId()
            ),

            requestUri,

            "Analysis Job 요청: "
                + analysisType
                + ", Version="
                + analysisVersion
                + ", Status=PENDING",

            ipAddress
        );


        return AnalysisJobCommandResponse.from(
            saved,
            null
        );
    }


    /**
     * FAILED Job 재실행 요청.
     *
     * 기존 FAILED 행은 수정하지 않고
     * 새로운 PENDING Job을 생성한다.
     */
    @Transactional
    public AnalysisJobCommandResponse retryJob(
            Long failedJobId,
            AnalysisJobRetryRequest request,
            String loginId,
            String requestUri,
            String ipAddress) {

        AnalysisJob failedJob =
            getJobEntity(
                failedJobId
            );


        if (
            !"FAILED".equals(
                failedJob.getStatus()
            )
        ) {

            throw new IllegalStateException(
                "FAILED 상태의 Job만 재실행 요청할 수 있습니다."
            );
        }


        Long datasetId =
            failedJob
                .getDataset()
                .getDatasetId();


        validateActiveDataset(
            datasetId
        );


        String hdfsOutputPath =
            normalizeRequired(
                request.hdfsOutputPath(),
                "hdfsOutputPath"
            );


        validateOutputPathNotUsed(
            hdfsOutputPath
        );


        AppUser requester =
            getRequester(
                loginId
            );


        AnalysisJob retryJob =
            AnalysisJob.createPending(

                failedJob.getDataset(),

                requester.getUserId(),

                failedJob.getAnalysisType(),

                failedJob.getAnalysisVersion(),

                failedJob.getHdfsInputPath(),

                hdfsOutputPath
            );


        AnalysisJob saved =
            repository.save(
                retryJob
            );


        /*
         * FAILED Job에 대해 사용자가 재실행을 요청한 시점이므로
         * JOB_RETRY Audit을 남긴다.
         *
         * 새 PENDING Job이 실제 실행될 때는
         * AnalysisJobExecutionService에서 JOB_RUN이 별도로 기록된다.
         */
        auditLogService.record(

            requester.getUserId(),

            "JOB_RETRY",

            "ANALYSIS_JOB",

            String.valueOf(
                failedJobId
            ),

            requestUri,

            "FAILED Job 재실행 요청: 기존 Job #"
                + failedJobId
                + " → 신규 Job #"
                + saved.getJobId()
                + ", Status=PENDING",

            ipAddress
        );


        return AnalysisJobCommandResponse.from(
            saved,
            failedJobId
        );
    }


    /**
     * Job Entity 조회.
     */
    private AnalysisJob getJobEntity(
            Long jobId) {

        return repository
            .findById(
                jobId
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "ANALYSIS_JOB을 찾을 수 없습니다. jobId="
                    + jobId
                )
            );
    }


    /**
     * 논리 삭제되지 않은 Dataset만 허용.
     */
    private void validateActiveDataset(
            Long datasetId) {

        if (
            datasetId == null
            ||
            datasetId <= 0
        ) {

            throw new IllegalArgumentException(
                "datasetId는 1 이상의 값이어야 합니다."
            );
        }


        int count =
            repository.countActiveDataset(
                datasetId
            );


        if (count == 0) {

            throw new IllegalArgumentException(
                "사용 가능한 Dataset을 찾을 수 없습니다. datasetId="
                + datasetId
            );
        }
    }


    /**
     * 실제 지원하는 분석 유형인지 검증.
     */
    private String normalizeAnalysisType(
            String analysisType) {

        String normalized =
            normalizeRequired(
                analysisType,
                "analysisType"
            )
            .toUpperCase();


        if (
            !SUPPORTED_ANALYSIS_TYPES
                .contains(
                    normalized
                )
        ) {

            throw new IllegalArgumentException(
                "지원하지 않는 analysisType입니다: "
                + normalized
            );
        }


        return normalized;
    }


    /**
     * HDFS Output 경로 중복 방지.
     */
    private void validateOutputPathNotUsed(
            String hdfsOutputPath) {

        if (
            repository.existsByHdfsOutputPath(
                hdfsOutputPath
            )
        ) {

            throw new IllegalArgumentException(
                "이미 사용된 HDFS Output 경로입니다: "
                + hdfsOutputPath
            );
        }
    }


    /**
     * 현재 로그인 사용자 조회.
     */
    private AppUser getRequester(
            String loginId) {

        return appUserRepository
            .findByLoginId(
                loginId
            )
            .orElseThrow(() ->
                new IllegalStateException(
                    "현재 로그인 사용자를 찾을 수 없습니다."
                )
            );
    }


    /**
     * 필수 문자열 정규화.
     */
    private String normalizeRequired(
            String value,
            String fieldName) {

        if (
            value == null
            ||
            value.isBlank()
        ) {

            throw new IllegalArgumentException(
                fieldName
                + "은(는) 필수입니다."
            );
        }


        return value.trim();
    }
}