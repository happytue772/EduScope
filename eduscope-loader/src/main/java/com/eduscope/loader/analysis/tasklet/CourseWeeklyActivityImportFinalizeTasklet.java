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
import com.eduscope.loader.analysis.repository.CourseWeeklyActivityStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;

/**
 * COURSE_WEEKLY_ACTIVITY_STAT 적재 건수를 검증하고
 * 성공 시 RESULT_IMPORTED_YN을 Y로 변경한다.
 */
@Component
@StepScope
public class CourseWeeklyActivityImportFinalizeTasklet
        implements Tasklet {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;
    private final AnalysisJobRepository analysisJobRepository;

    // Processor와 동일한 Job Parameter를 사용한다.
    private final Long requestedJobId;
    private final CourseWeeklyActivityStatRepository statRepository;

    public CourseWeeklyActivityImportFinalizeTasklet(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            CourseWeeklyActivityStatRepository statRepository,
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

            if (!"COURSE_WEEKLY_ACTIVITY".equals(job.getAnalysisType())) {
                throw new IllegalStateException(
                    "COURSE_WEEKLY_ACTIVITY 분석 Job이 아닙니다."
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
                    "COURSE_WEEKLY_ACTIVITY",
                    "SUCCESS"
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "COURSE_WEEKLY_ACTIVITY ANALYSIS_JOB 없음"
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
                "COURSE_WEEKLY_ACTIVITY 적재 건수 불일치"
                + " / expected="
                + expectedCount
                + " / actual="
                + actualCount
            );
        }

        job.setResultImportedYn("Y");

        analysisJobRepository.save(job);

        System.out.println(
            "[COURSE_WEEKLY_ACTIVITY] 적재 검증 완료"
            + " / jobId="
            + job.getJobId()
            + " / rows="
            + actualCount
            + " / RESULT_IMPORTED_YN=Y"
        );

        return RepeatStatus.FINISHED;
    }
}