package com.eduscope.web.analysis.dto;

import java.time.LocalDateTime;

import com.eduscope.web.analysis.entity.AnalysisJob;

/**
 * Job 생성 / 재실행 요청 응답.
 */
public record AnalysisJobCommandResponse(

    Long jobId,

    Long previousJobId,

    Long datasetId,

    Long requestedBy,

    String analysisType,

    String status,

    String analysisVersion,

    String hdfsInputPath,

    String hdfsOutputPath,

    LocalDateTime requestedAt

) {

    public static AnalysisJobCommandResponse from(
            AnalysisJob job,
            Long previousJobId) {

        return new AnalysisJobCommandResponse(

            job.getJobId(),

            previousJobId,

            job.getDataset()
                .getDatasetId(),

            job.getRequestedBy(),

            job.getAnalysisType(),

            job.getStatus(),

            job.getAnalysisVersion(),

            job.getHdfsInputPath(),

            job.getHdfsOutputPath(),

            job.getRequestedAt()
        );
    }
}