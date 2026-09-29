package com.eduscope.web.job.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.job.dto.AnalysisJobResponse;
import com.eduscope.web.job.repository.AnalysisJobViewRepository;


/**
 * Analysis Job 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class AnalysisJobViewService {

    private final AnalysisJobViewRepository repository;

    public AnalysisJobViewService(
            AnalysisJobViewRepository repository) {

        this.repository = repository;
    }


    public AnalysisJobResponse getJobs(
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


        List<AnalysisJobResponse.JobItem> jobs =
            repository.findJobs(
                datasetId
            );


        long successCount =
            jobs.stream()
                .filter(
                    job ->
                        "SUCCESS".equals(
                            job.getStatus()
                        )
                )
                .count();


        long failedCount =
            jobs.stream()
                .filter(
                    job ->
                        "FAILED".equals(
                            job.getStatus()
                        )
                )
                .count();


        long importedCount =
            jobs.stream()
                .filter(
                    job ->
                        "Y".equals(
                            job.getResultImportedYn()
                        )
                )
                .count();


        AnalysisJobResponse.Summary summary =
            new AnalysisJobResponse.Summary(
                (long) jobs.size(),
                successCount,
                failedCount,
                importedCount
            );


        return new AnalysisJobResponse(
            summary,
            jobs
        );
    }
}