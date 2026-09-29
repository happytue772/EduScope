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
import com.eduscope.loader.analysis.repository.AssessmentStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;

/**
 * ASSESSMENT_STAT 적재 건수를 검증한 뒤
 * RESULT_IMPORTED_YN을 Y로 변경한다.
 *
 * jobId가 전달되면 Processor와 동일한 Job을 검증하고,
 * jobId가 없으면 기존 최신 SUCCESS 방식으로 fallback한다.
 */
@Component
@StepScope
public class AssessmentImportFinalizeTasklet
        implements Tasklet {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;
    private final AnalysisJobRepository analysisJobRepository;
    private final AssessmentStatRepository statRepository;

    /*
     * Processor와 동일한 jobId Job Parameter를 사용한다.
     *
     * null이면 기존 최신 SUCCESS 방식으로 fallback한다.
     */
    private final Long requestedJobId;

    public AssessmentImportFinalizeTasklet(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            AssessmentStatRepository statRepository,
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
         * jobId가 있으면 해당 ANALYSIS_JOB을 정확히 조회한다.
         */
        if (requestedJobId != null) {

            job = analysisJobRepository
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
             * Assessment 적재 Job인지 검증한다.
             */
            if (!"ASSESSMENT".equals(
                    job.getAnalysisType())) {

                throw new IllegalStateException(
                    "ASSESSMENT 분석 Job이 아닙니다."
                    + " / jobId="
                    + job.getJobId()
                    + " / analysisType="
                    + job.getAnalysisType()
                );
            }

            /*
             * Hadoop 분석 성공 상태인지 검증한다.
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
             *
             * DatasetProperties를 이용하여 Dataset을 찾고,
             * 가장 최근 SUCCESS ASSESSMENT Job을 사용한다.
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

            job = analysisJobRepository
                .findFirstByDatasetAndAnalysisTypeAndStatusOrderByJobIdDesc(
                    dataset,
                    "ASSESSMENT",
                    "SUCCESS"
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "ASSESSMENT ANALYSIS_JOB 없음"
                    )
                );
        }

        /*
         * 해당 Job으로 실제 Oracle에 적재된
         * ASSESSMENT_STAT 건수를 조회한다.
         */
        long actualCount =
            statRepository.countByJobId(
                job.getJobId()
            );

        /*
         * Hadoop MapReduce가 생성한
         * OUTPUT_RECORD_COUNT와 비교한다.
         */
        long expectedCount =
            job.getOutputRecordCount();

        /*
         * Hadoop 결과 건수와 Oracle 실제 적재 건수가
         * 완전히 동일하지 않으면 Imported=Y로 바꾸지 않는다.
         */
        if (actualCount != expectedCount) {

            throw new IllegalStateException(
                "ASSESSMENT 적재 건수 불일치"
                + " / jobId="
                + job.getJobId()
                + " / expected="
                + expectedCount
                + " / actual="
                + actualCount
            );
        }

        /*
         * 실제 적재 검증이 성공한 경우에만
         * RESULT_IMPORTED_YN을 Y로 변경한다.
         */
        job.setResultImportedYn("Y");

        
        analysisJobRepository.save(job);

        System.out.println(
            "[ASSESSMENT] 적재 검증 완료"
            + " / jobId="
            + job.getJobId()
            + " / rows="
            + actualCount
            + " / RESULT_IMPORTED_YN=Y"
        );

        return RepeatStatus.FINISHED;
    }
}