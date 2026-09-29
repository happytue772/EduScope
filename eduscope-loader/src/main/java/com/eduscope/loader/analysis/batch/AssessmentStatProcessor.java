package com.eduscope.loader.analysis.batch;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eduscope.loader.analysis.dto.AssessmentStatCsvRow;
import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.entity.AssessmentStat;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.analysis.repository.AssessmentStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.entity.Assessment;
import com.eduscope.loader.reference.repository.AssessmentRepository;

/**
 * SOURCE_ASSESSMENT_ID를 실제 ASSESSMENT_ID로 변환하여
 * ASSESSMENT_STAT Entity를 생성한다.
 *
 * jobId가 전달되면 해당 ANALYSIS_JOB을 직접 사용하고,
 * jobId가 없으면 기존 최신 SUCCESS ASSESSMENT Job 방식을 유지한다.
 */
@Component
@StepScope
public class AssessmentStatProcessor
        implements ItemProcessor<
            AssessmentStatCsvRow,
            AssessmentStat> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final AnalysisJobRepository analysisJobRepository;
    private final AssessmentStatRepository statRepository;
    private final AssessmentRepository assessmentRepository;

    /*
     * 신규:
     * 명시적으로 적재할 ANALYSIS_JOB의 JOB_ID.
     *
     * null이면 기존 최신 SUCCESS Job 조회 방식으로 fallback한다.
     */
    private final Long requestedJobId;

    private Dataset dataset;
    private AnalysisJob analysisJob;

    /*
     * SOURCE_ASSESSMENT_ID 조회 반복을 줄이기 위한 Cache.
     */
    private final Map<Long, Assessment> assessmentCache =
            new HashMap<>();

    /*
     * 이미 ASSESSMENT_STAT에 존재하는 Assessment ID와
     * 현재 TSV 처리 중 이미 처리된 ID를 함께 관리한다.
     *
     * 동일 Job 재실행 및 TSV 내부 중복을 방지한다.
     */
    private Set<Long> existingAssessmentIds;

    public AssessmentStatProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            AssessmentStatRepository statRepository,
            AssessmentRepository assessmentRepository,
            @Value("#{jobParameters['jobId']}")
            Long requestedJobId) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.analysisJobRepository = analysisJobRepository;
        this.statRepository = statRepository;
        this.assessmentRepository = assessmentRepository;
        this.requestedJobId = requestedJobId;
    }

    @Override
    public AssessmentStat process(
            AssessmentStatCsvRow item) {

        initialize();

        /*
         * OULAD SOURCE_ASSESSMENT_ID
         * → Oracle 실제 ASSESSMENT_ID 변환.
         */
        Assessment assessment =
            assessmentCache.computeIfAbsent(
                item.getSourceAssessmentId(),
                sourceId -> assessmentRepository
                    .findByDatasetAndSourceAssessmentId(
                        dataset,
                        sourceId
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "ASSESSMENT 없음"
                            + " / SOURCE_ASSESSMENT_ID="
                            + sourceId
                        )
                    )
            );

        Long assessmentId =
                assessment.getAssessmentId();

        /*
         * 기존 기능 유지.
         *
         * DB에 이미 존재하거나 현재 TSV에서
         * 이미 처리한 Assessment라면 Writer로 넘기지 않는다.
         */
        if (!existingAssessmentIds.add(
                assessmentId)) {

            return null;
        }

        AssessmentStat stat =
                new AssessmentStat();

        /*
         * jobId Parameter가 있으면 명시적으로 선택한 Job,
         * 없으면 기존 최신 SUCCESS Job의 JOB_ID가 들어간다.
         */
        stat.setJobId(
            analysisJob.getJobId()
        );

        stat.setAssessmentId(
            assessmentId
        );

        stat.setSubmissionCount(
            item.getSubmissionCount()
        );

        stat.setAvgScore(
            item.getAvgScore()
        );

        stat.setFailCount(
            item.getFailCount()
        );

        /*
         * MapReduce가 생성한 0.0168 등을
         * 임의로 1.68로 변환하지 않는다.
         */
        stat.setFailRate(
            item.getFailRate()
        );

        stat.setBankedCount(
            item.getBankedCount()
        );

        stat.setAvgSubmissionDay(
            item.getAvgSubmissionDay()
        );

        stat.setCreatedAt(
            LocalDateTime.now()
        );

        return stat;
    }

    /**
     * Dataset / AnalysisJob / 기존 적재 데이터를
     * 최초 1회 초기화한다.
     */
    private void initialize() {

        /*
         * Analysis Job 선택.
         *
         * 신규 방식:
         * jobId가 있으면 해당 Job을 정확히 조회한다.
         *
         * 기존 방식:
         * jobId가 없으면 기존 최신 SUCCESS ASSESSMENT Job을 사용한다.
         */
        if (analysisJob == null) {

            if (requestedJobId != null) {

                analysisJob = analysisJobRepository
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
                 * 잘못된 분석 유형의 Job에
                 * Assessment 결과가 적재되는 것을 방지한다.
                 */
                if (!"ASSESSMENT".equals(
                        analysisJob.getAnalysisType())) {

                    throw new IllegalStateException(
                        "ASSESSMENT 분석 Job이 아닙니다."
                        + " / jobId="
                        + analysisJob.getJobId()
                        + " / analysisType="
                        + analysisJob.getAnalysisType()
                    );
                }

                /*
                 * Hadoop 분석이 성공한 Job만
                 * Oracle 결과 적재 대상으로 사용한다.
                 */
                if (!"SUCCESS".equals(
                        analysisJob.getStatus())) {

                    throw new IllegalStateException(
                        "SUCCESS 상태의 ANALYSIS_JOB이 아닙니다."
                        + " / jobId="
                        + analysisJob.getJobId()
                        + " / status="
                        + analysisJob.getStatus()
                    );
                }

                /*
                 * 명시적으로 선택한 Job이 속한 Dataset을
                 * Assessment 매핑 기준으로 사용한다.
                 */
                dataset =
                    analysisJob.getDataset();

            } else {

                /*
                 * 기존 방식 유지.
                 *
                 * DatasetProperties 기준으로 Dataset을 조회한다.
                 */
                if (dataset == null) {

                    dataset = datasetRepository
                        .findBySourceNameAndDatasetVersion(
                            datasetProperties.getSourceName(),
                            datasetProperties.getDatasetVersion()
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "DATASET을 찾을 수 없습니다."
                            )
                        );
                }

                /*
                 * 기존 최신 SUCCESS ASSESSMENT Job
                 * 자동 선택 방식을 fallback으로 유지한다.
                 */
                analysisJob = analysisJobRepository
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
        }

        /*
         * 기존 적재된 Assessment ID 조회.
         *
         * 같은 Job을 다시 실행해도 이미 존재하는 데이터는
         * 다시 INSERT하지 않는다.
         */
        if (existingAssessmentIds == null) {

            existingAssessmentIds =
                new HashSet<>(
                    statRepository
                        .findAssessmentIdsByJobId(
                            analysisJob.getJobId()
                        )
                );
        }
    }
}