package com.eduscope.loader.analysis.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.analysis.repository.StudentLearningSummaryStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;

/**
 * STUDENT_LEARNING_SUMMARY_STAT 적재 건수 검증.
 */
@Component
@StepScope
public class StudentLearningSummaryImportFinalizeTasklet
        implements Tasklet {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final AnalysisJobRepository analysisJobRepository;

    // Processor와 동일한 Job Parameter를 사용한다.
    private final Long requestedJobId;

    private final StudentLearningSummaryStatRepository statRepository;

    public StudentLearningSummaryImportFinalizeTasklet(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            StudentLearningSummaryStatRepository statRepository,
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

            if (!"STUDENT_LEARNING_SUMMARY".equals(job.getAnalysisType())) {
                throw new IllegalStateException(
                    "STUDENT_LEARNING_SUMMARY 분석 Job이 아닙니다."
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
                    "STUDENT_LEARNING_SUMMARY",
                    "SUCCESS"
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "STUDENT_LEARNING_SUMMARY ANALYSIS_JOB 없음"
                    )
                );
        }

        long actualCount =
                statRepository.countByJobId(
                    job.getJobId()
                );

        long expectedCount =
                job.getOutputRecordCount();

        if (actualCount != expectedCount) {

            throw new IllegalStateException(
                "STUDENT_LEARNING_SUMMARY 적재 건수 불일치"
                + " / expected="
                + expectedCount
                + " / actual="
                + actualCount
            );
        }

        job.setResultImportedYn("Y");

        analysisJobRepository.save(job);

        System.out.println(
            "[STUDENT_LEARNING_SUMMARY] 적재 검증 완료"
            + " / jobId="
            + job.getJobId()
            + " / rows="
            + actualCount
            + " / RESULT_IMPORTED_YN=Y"
        );

        return RepeatStatus.FINISHED;
    }
}