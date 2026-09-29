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
import com.eduscope.loader.analysis.repository.CourseResultStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;

@Component
@StepScope
public class CourseResultImportFinalizeTasklet implements Tasklet {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties properties;
    private final AnalysisJobRepository jobRepository;

    // Processor와 동일한 Job Parameter를 사용한다.
    private final Long requestedJobId;
    private final CourseResultStatRepository statRepository;

    public CourseResultImportFinalizeTasklet(
            DatasetRepository datasetRepository,
            DatasetProperties properties,
            AnalysisJobRepository jobRepository,
            CourseResultStatRepository statRepository,
            @Value("#{jobParameters['jobId']}")
            Long requestedJobId
) {

        this.datasetRepository = datasetRepository;
        this.properties = properties;
        this.jobRepository = jobRepository;
        this.statRepository = statRepository;
        this.requestedJobId = requestedJobId;
    }

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) {

        AnalysisJob job;

        if (requestedJobId != null) {
            job = jobRepository
                .findById(requestedJobId)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "ANALYSIS_JOB을 찾을 수 없습니다."
                        + " / jobId=" + requestedJobId
                    )
                );

            if (!"COURSE_RESULT".equals(job.getAnalysisType())) {
                throw new IllegalStateException(
                    "COURSE_RESULT 분석 Job이 아닙니다."
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
                		properties.getSourceName(),
                		properties.getDatasetVersion()
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "DATASET을 찾을 수 없습니다."
                    )
                );

            job = jobRepository
                .findFirstByDatasetAndAnalysisTypeAndStatusOrderByJobIdDesc(
                    dataset,
                    "COURSE_RESULT",
                    "SUCCESS"
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "COURSE_RESULT ANALYSIS_JOB 없음"
                    )
                );
        }

        long actual =
                statRepository.countByJobId(job.getJobId());

        if (actual != job.getOutputRecordCount()) {
            throw new IllegalStateException(
                "COURSE_RESULT 건수 불일치"
                + " / expected=" + job.getOutputRecordCount()
                + " / actual=" + actual
            );
        }

        job.setResultImportedYn("Y");
        jobRepository.save(job);

        return RepeatStatus.FINISHED;
    }
}