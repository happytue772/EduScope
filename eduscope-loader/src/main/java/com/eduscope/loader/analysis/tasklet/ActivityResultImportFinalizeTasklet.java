package com.eduscope.loader.analysis.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.repository.ActivityResultStatRepository;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;

/**
 * ACTIVITY_RESULT_STAT 적재 완료 후
 * 실제 DB 건수를 검증한다.
 *
 * 건수가 정확할 때만
 * RESULT_IMPORTED_YN = Y 로 변경한다.
 */
@Component
@StepScope
public class ActivityResultImportFinalizeTasklet
        implements Tasklet {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final AnalysisJobRepository analysisJobRepository;

    // Processor와 동일한 Job Parameter를 사용한다.
    private final Long requestedJobId;
    private final ActivityResultStatRepository statRepository;

    public ActivityResultImportFinalizeTasklet(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            ActivityResultStatRepository statRepository,
            @Value("#{jobParameters['jobId']}")
            Long requestedJobId
) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.analysisJobRepository = analysisJobRepository;
        this.statRepository = statRepository;
        this.requestedJobId = requestedJobId;
    }

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) {

        AnalysisJob job;

        if (requestedJobId != null) {
            job = analysisJobRepository
                .findById(requestedJobId)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "ANALYSIS_JOB을 찾을 수 없습니다."
                        + " / jobId=" + requestedJobId
                    )
                );

            if (!"ACTIVITY_RESULT".equals(job.getAnalysisType())) {
                throw new IllegalStateException(
                    "ACTIVITY_RESULT 분석 Job이 아닙니다."
                    + " / jobId=" + job.getJobId()
                    + " / analysisType=" + job.getAnalysisType()
                );
            }

            if (!"SUCCESS".equals(job.getStatus())) {
                throw new IllegalStateException(
                    "SUCCESS 상태의 ANALYSIS_JOB이 아닙니다."
                    + " / jobId=" + job.getJobId()
                    + " / status=" + job.getStatus()
                );
            }

        } else {
            Dataset dataset = datasetRepository
                .findBySourceNameAndDatasetVersion(
                    datasetProperties.getSourceName(),
                    datasetProperties.getDatasetVersion()
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "DATASET을 찾을 수 없습니다."
                    )
                );

            job = analysisJobRepository
                .findFirstByDatasetAndAnalysisTypeAndStatusOrderByJobIdDesc(
                    dataset,
                    "ACTIVITY_RESULT",
                    "SUCCESS"
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "ACTIVITY_RESULT ANALYSIS_JOB 없음"
                    )
                );
        }

        long actualCount =
                statRepository.countByJobId(
                    job.getJobId()
                );

        /*
         * MapReduce 결과 기준 예상 행 수.
         * 현재 실제 값은 88.
         */
        long expectedCount =
                job.getOutputRecordCount();

        if (actualCount != expectedCount) {

            throw new IllegalStateException(
                "ACTIVITY_RESULT 적재 건수 불일치"
                + " / expected="
                + expectedCount
                + " / actual="
                + actualCount
            );
        }

        /*
         * 실제 건수 검증 성공 후에만 Y 처리.
         */
        job.setResultImportedYn("Y");

        analysisJobRepository.save(job);

        System.out.println(
            "[ACTIVITY_RESULT] 적재 검증 완료"
            + " / jobId="
            + job.getJobId()
            + " / rows="
            + actualCount
            + " / RESULT_IMPORTED_YN=Y"
        );

        return RepeatStatus.FINISHED;
    }
}