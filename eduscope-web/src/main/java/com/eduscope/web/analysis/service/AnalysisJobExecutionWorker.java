package com.eduscope.web.analysis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.eduscope.web.analysis.execution.AnalysisExecutionPlan;
import com.eduscope.web.analysis.execution.AnalysisExecutionResult;
import com.eduscope.web.analysis.execution.AnalysisJobExecutor;
/**
 * 실제 Hadoop 실행 Worker.
 *
 * 긴 SSH/MapReduce 작업 동안
 * DB Transaction을 유지하지 않는다.
 */
@Service
public class AnalysisJobExecutionWorker {

    private static final Logger log =
        LoggerFactory.getLogger(
            AnalysisJobExecutionWorker.class
        );

    private final AnalysisJobExecutor executor;

    private final AnalysisJobStatusService statusService;


    public AnalysisJobExecutionWorker(
            AnalysisJobExecutor executor,
            AnalysisJobStatusService statusService) {

        this.executor =
            executor;

        this.statusService =
            statusService;
    }


    @Async(
        "analysisJobTaskExecutor"
    )
    public void executeAsync(
            Long jobId,
            AnalysisExecutionPlan plan) {

        log.info(
            "Analysis Job 실행 시작: jobId={}, analysisType={}",
            jobId,
            plan.getAnalysisType()
        );

        try {

            AnalysisExecutionResult result =
                executor.execute(
                    plan
                );


            if (
                result.isSuccess()
            ) {

                statusService.markSuccess(
                    jobId,
                    result.getOutputRecordCount(),
                    result.getProcessingTimeMs()
                );

                log.info(
                    "Analysis Job 실행 성공: jobId={}, outputRecordCount={}, processingTimeMs={}",
                    jobId,
                    result.getOutputRecordCount(),
                    result.getProcessingTimeMs()
                );

                return;
            }


            statusService.markFailed(
                jobId,
                result.getErrorStep(),
                result.getMessage(),
                result.getProcessingTimeMs()
            );

            log.warn(
                "Analysis Job 실행 실패: jobId={}, errorStep={}, exitCode={}, processingTimeMs={}",
                jobId,
                result.getErrorStep(),
                result.getExitCode(),
                result.getProcessingTimeMs()
            );


        } catch (Exception ex) {

            log.error(
                "Analysis Job 실행 중 예외 발생: jobId={}, analysisType={}",
                jobId,
                plan.getAnalysisType(),
                ex
            );

            statusService.markFailed(
                jobId,
                "EXECUTOR_INTERNAL",
                ex.getClass().getSimpleName()
                    + ": "
                    + ex.getMessage(),
                0L
            );
        }
    }
}
