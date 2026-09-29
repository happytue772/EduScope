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
import com.eduscope.loader.analysis.repository.CourseActivityStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;

/**
 * COURSE_ACTIVITY_STAT 실제 적재 건수를 검증한 뒤
 * RESULT_IMPORTED_YN을 Y로 변경한다.
 *
 * jobId가 전달되면 정확한 Job을 검증하고,
 * 없으면 기존 최신 SUCCESS 방식을 유지한다.
 */
@Component
@StepScope
public class CourseActivityImportFinalizeTasklet
        implements Tasklet {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final AnalysisJobRepository analysisJobRepository;
    private final CourseActivityStatRepository statRepository;

    /*
     * Processor와 동일한 Job Parameter 사용.
     */
    private final Long requestedJobId;

    public CourseActivityImportFinalizeTasklet(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            CourseActivityStatRepository statRepository,
            @Value("#{jobParameters['jobId']}")
            Long requestedJobId) {

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

        /*
         * 신규 방식:
         * jobId가 있으면 정확한 Job 조회.
         */
        if (requestedJobId != null) {

            job =
                analysisJobRepository
                    .findById(
                        requestedJobId
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "ANALYSIS_JOB을 찾을 수 없습니다."
                            + " / jobId="
                            + requestedJobId
                        )
                    );

            /*
             * 분석 유형 검증.
             */
            if (!"COURSE_ACTIVITY".equals(
                    job.getAnalysisType())) {

                throw new IllegalStateException(
                    "COURSE_ACTIVITY 분석 Job이 아닙니다."
                    + " / jobId="
                    + job.getJobId()
                    + " / analysisType="
                    + job.getAnalysisType()
                );
            }

            /*
             * SUCCESS Job만 Oracle 적재 완료 처리.
             */
            if (!"SUCCESS".equals(
                    job.getStatus())) {

                throw new IllegalStateException(
                    "SUCCESS 상태의 ANALYSIS_JOB이 아닙니다."
                    + " / jobId="
                    + job.getJobId()
                    + " / status="
                    + job.getStatus()
                );
            }

        } else {

            /*
             * 기존 방식 유지.
             */
            Dataset dataset =
                datasetRepository
                    .findBySourceNameAndDatasetVersion(
                        datasetProperties.getSourceName(),
                        datasetProperties.getDatasetVersion()
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "DATASET을 찾을 수 없습니다."
                        )
                    );

            job =
                analysisJobRepository
                    .findFirstByDatasetAndAnalysisTypeAndStatusOrderByJobIdDesc(
                        dataset,
                        "COURSE_ACTIVITY",
                        "SUCCESS"
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "COURSE_ACTIVITY ANALYSIS_JOB 없음"
                        )
                    );
        }

        /*
         * Oracle 실제 적재 건수.
         */
        long actualCount =
            statRepository.countByJobId(
                job.getJobId()
            );

        /*
         * Hadoop MapReduce 결과 건수.
         */
        long expectedCount =
            job.getOutputRecordCount();

        /*
         * Hadoop 결과와 Oracle 적재 건수가
         * 완전히 동일해야 Imported=Y 처리.
         */
        if (actualCount != expectedCount) {

            throw new IllegalStateException(
                "COURSE_ACTIVITY 적재 건수 불일치"
                + " / jobId="
                + job.getJobId()
                + " / expected="
                + expectedCount
                + " / actual="
                + actualCount
            );
        }

        job.setResultImportedYn(
            "Y"
        );

        analysisJobRepository.save(
            job
        );

        System.out.println(
            "[COURSE_ACTIVITY] 적재 검증 완료"
            + " / jobId="
            + job.getJobId()
            + " / rows="
            + actualCount
            + " / RESULT_IMPORTED_YN=Y"
        );

        return RepeatStatus.FINISHED;
    }
}