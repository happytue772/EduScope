package com.eduscope.web.analysis.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.AnalysisJobCommandResponse;
import com.eduscope.web.analysis.dto.AnalysisJobCreateRequest;
import com.eduscope.web.analysis.dto.AnalysisJobExecutionResponse;
import com.eduscope.web.analysis.dto.AnalysisJobResponse;
import com.eduscope.web.analysis.dto.AnalysisJobRetryRequest;
import com.eduscope.web.analysis.service.AnalysisJobExecutionService;
import com.eduscope.web.analysis.service.AnalysisJobService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


/**
 * ANALYSIS_JOB REST API.
 *
 * 담당 기능:
 *
 * 1. 전체 Job 조회
 * 2. Job 단건 조회
 * 3. 분석 유형별 조회
 * 4. 신규 PENDING Job 생성
 * 5. FAILED Job 재실행 요청
 * 6. PENDING Job 실제 Hadoop 실행
 *
 * 실제 MapReduce 실행 자체는
 * AnalysisJobExecutionService 이하 계층이 담당한다.
 */
@RestController
@RequestMapping("/api/analysis-jobs")
public class AnalysisJobController {

    /*
     * Job 조회 / 생성 / 재실행 요청 담당.
     */
    private final AnalysisJobService service;


    /*
     * 실제 Hadoop 실행 시작 담당.
     */
    private final AnalysisJobExecutionService
        executionService;


    /**
     * 생성자 주입.
     */
    public AnalysisJobController(
            AnalysisJobService service,
            AnalysisJobExecutionService executionService) {

        this.service =
            service;

        this.executionService =
            executionService;
    }


    /**
     * 전체 Analysis Job 조회.
     *
     * GET
     * /api/analysis-jobs
     */
    @GetMapping
    public List<AnalysisJobResponse> getJobs() {

        return service.getJobs();
    }


    /**
     * Analysis Job 단건 조회.
     *
     * GET
     * /api/analysis-jobs/{jobId}
     */
    @GetMapping("/{jobId}")
    public AnalysisJobResponse getJob(
            @PathVariable Long jobId) {

        return service.getJob(
            jobId
        );
    }


    /**
     * 분석 유형별 Job 조회.
     *
     * GET
     * /api/analysis-jobs/type
     * ?analysisType=STUDENT_ACTIVITY
     */
    @GetMapping("/type")
    public List<AnalysisJobResponse>
            getJobsByType(
                @RequestParam
                String analysisType) {

        return service.getJobsByType(
            analysisType
        );
    }


    /**
     * 신규 Analysis Job 요청.
     *
     * 실제 Hadoop 실행을 즉시 수행하지 않고
     * Oracle ANALYSIS_JOB에
     * PENDING 상태 Job을 생성한다.
     *
     * POST
     * /api/analysis-jobs
     */
    @PostMapping
    @ResponseStatus(
        HttpStatus.CREATED
    )
    public AnalysisJobCommandResponse createJob(

            @Valid
            @RequestBody
            AnalysisJobCreateRequest request,

            Principal principal,

            HttpServletRequest httpRequest) {

        return service.createJob(

            request,

            principal.getName(),

            httpRequest.getRequestURI(),

            httpRequest.getRemoteAddr()
        );
    }


    /**
     * FAILED Job 재실행 요청.
     *
     * 기존 FAILED Job은 수정하지 않는다.
     *
     * 기존 실패 이력을 보존하고
     * 새로운 PENDING Job을 생성한다.
     *
     * POST
     * /api/analysis-jobs/{jobId}/retry
     */
    @PostMapping("/{jobId}/retry")
    @ResponseStatus(
        HttpStatus.CREATED
    )
    public AnalysisJobCommandResponse retryJob(

            @PathVariable
            Long jobId,

            @Valid
            @RequestBody
            AnalysisJobRetryRequest request,

            Principal principal,

            HttpServletRequest httpRequest) {

        return service.retryJob(

            jobId,

            request,

            principal.getName(),

            httpRequest.getRequestURI(),

            httpRequest.getRemoteAddr()
        );
    }


    /**
     * PENDING Job 실제 실행 시작.
     *
     * 처리 흐름:
     *
     * PENDING
     *   ↓
     * RUNNING
     *   ↓
     * SSH
     *   ↓
     * VMware Ubuntu
     *   ↓
     * Java MapReduce
     *   ↓
     * SUCCESS / FAILED
     *
     * Hadoop 작업은 비동기로 실행된다.
     *
     * POST
     * /api/analysis-jobs/{jobId}/execute
     */
    @PostMapping("/{jobId}/execute")
    @ResponseStatus(
        HttpStatus.ACCEPTED
    )
    public AnalysisJobExecutionResponse executeJob(

            @PathVariable
            Long jobId,

            Principal principal,

            HttpServletRequest httpRequest) {

        return executionService.startExecution(

            jobId,

            principal.getName(),

            httpRequest.getRequestURI(),

            httpRequest.getRemoteAddr()
        );
    }
}