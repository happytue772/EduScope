package com.eduscope.web.analysis.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.entity.AnalysisJob;
import com.eduscope.web.analysis.repository.AnalysisJobRepository;

/**
 * 긴 Hadoop 실행과 DB Transaction을 분리하기 위한
 * Analysis Job 상태 전용 Service.
 */
@Service
public class AnalysisJobStatusService {

    private final AnalysisJobRepository repository;


    public AnalysisJobStatusService(
            AnalysisJobRepository repository) {

        this.repository =
            repository;
    }


    @Transactional
    public void markSuccess(
            Long jobId,
            long outputRecordCount,
            long processingTimeMs) {

        AnalysisJob job =
            getJob(
                jobId
            );


        job.markSuccess(
            outputRecordCount,
            processingTimeMs
        );
    }


    @Transactional
    public void markFailed(
            Long jobId,
            String errorStep,
            String errorMessage,
            long processingTimeMs) {

        AnalysisJob job =
            getJob(
                jobId
            );


        job.markFailed(
            errorStep,
            errorMessage,
            processingTimeMs
        );
    }


    private AnalysisJob getJob(
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
}