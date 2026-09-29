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

import com.eduscope.loader.analysis.dto.ActivityResultStatCsvRow;
import com.eduscope.loader.analysis.entity.ActivityResultStat;
import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.repository.ActivityResultStatRepository;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;

/**
 * activity-result.tsv 데이터를
 * ACTIVITY_RESULT_STAT Entity로 변환한다.
 */
@Component
@StepScope
public class ActivityResultStatProcessor
        implements ItemProcessor<
            ActivityResultStatCsvRow,
            ActivityResultStat> {

    private static final Set<String> ALLOWED_FINAL_RESULTS =
            Set.of(
                "Pass",
                "Fail",
                "Withdrawn",
                "Distinction"
            );

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final AnalysisJobRepository analysisJobRepository;
    private final ActivityResultStatRepository statRepository;

    private final CoursePresentationRepository courseRepository;

    // jobId 지정 시 정확한 ANALYSIS_JOB을 선택한다.
    private final Long requestedJobId;

    private Dataset dataset;
    private AnalysisJob analysisJob;

    /**
     * 동일 강의를 반복 조회하지 않도록 캐시한다.
     */
    private final Map<String, CoursePresentation> courseCache =
            new HashMap<>();

    /**
     * PK 중복 방지용.
     *
     * key =
     * COURSE_PRESENTATION_ID|FINAL_RESULT
     */
    private Set<String> existingKeys;

    public ActivityResultStatProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            ActivityResultStatRepository statRepository,
            CoursePresentationRepository courseRepository,
            @Value("#{jobParameters['jobId']}")
            Long requestedJobId
) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.analysisJobRepository = analysisJobRepository;
        this.statRepository = statRepository;
        this.courseRepository = courseRepository;
        this.requestedJobId = requestedJobId;
    }

    @Override
    public ActivityResultStat process(
            ActivityResultStatCsvRow item) {

        initialize();

        /*
         * DB CHECK:
         * Pass / Fail / Withdrawn / Distinction
         */
        if (!ALLOWED_FINAL_RESULTS.contains(
                item.getFinalResult())) {

            throw new IllegalArgumentException(
                "허용되지 않은 FINAL_RESULT: "
                + item.getFinalResult()
            );
        }

        String courseKey =
                item.getCodeModule()
                + "|"
                + item.getCodePresentation();

        /*
         * TSV의 AAA|2013J 등을
         * 실제 COURSE_PRESENTATION으로 연결한다.
         */
        CoursePresentation course =
                courseCache.computeIfAbsent(
                    courseKey,
                    key -> courseRepository
                        .findByDatasetAndCodeModuleAndCodePresentation(
                            dataset,
                            item.getCodeModule(),
                            item.getCodePresentation()
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "COURSE_PRESENTATION 없음: "
                                + courseKey
                            )
                        )
                );

        Long coursePresentationId =
                course.getCoursePresentationId();

        /*
         * ACTIVITY_RESULT_STAT PK:
         *
         * JOB_ID
         * + COURSE_PRESENTATION_ID
         * + FINAL_RESULT
         */
        String statKey =
                coursePresentationId
                + "|"
                + item.getFinalResult();

        /*
         * 기존 DB 데이터와
         * TSV 내부 중복을 모두 방지한다.
         */
        if (!existingKeys.add(statKey)) {
            return null;
        }

        ActivityResultStat stat =
                new ActivityResultStat();

        stat.setJobId(
            analysisJob.getJobId()
        );

        stat.setCoursePresentationId(
            coursePresentationId
        );

        stat.setFinalResult(
            item.getFinalResult()
        );

        stat.setStudentCount(
            item.getStudentCount()
        );

        stat.setTotalClickCount(
            item.getTotalClickCount()
        );

        stat.setAvgClickCount(
            item.getAvgClickCount()
        );

        stat.setAvgActiveDayCount(
            item.getAvgActiveDayCount()
        );

        /*
         * Oracle 적재 시점.
         */
        stat.setCreatedAt(
            LocalDateTime.now()
        );

        return stat;
    }

    /**
     * Job 실행 중 한 번만 필요한 기준정보를 초기화한다.
     */
    private void initialize() {

        if (analysisJob == null) {

            if (requestedJobId != null) {
                analysisJob = analysisJobRepository
                    .findById(requestedJobId)
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "ANALYSIS_JOB을 찾을 수 없습니다."
                            + " / jobId=" + requestedJobId
                        )
                    );

                if (!"ACTIVITY_RESULT".equals(analysisJob.getAnalysisType())) {
                    throw new IllegalStateException(
                        "ACTIVITY_RESULT 분석 Job이 아닙니다."
                        + " / jobId=" + analysisJob.getJobId()
                        + " / analysisType=" + analysisJob.getAnalysisType()
                    );
                }

                if (!"SUCCESS".equals(analysisJob.getStatus())) {
                    throw new IllegalStateException(
                        "SUCCESS 상태의 ANALYSIS_JOB이 아닙니다."
                        + " / jobId=" + analysisJob.getJobId()
                        + " / status=" + analysisJob.getStatus()
                    );
                }

                dataset = analysisJob.getDataset();

            } else {
                // 기존 최신 SUCCESS Job 선택 방식 유지.
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

                analysisJob = analysisJobRepository
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
        }

        if (existingKeys == null) {

            existingKeys =
                    new HashSet<>();

            /*
             * 이미 적재된 데이터를 한 번에 읽어서
             * 재실행 시 PK 충돌을 막는다.
             */
            statRepository
                .findAllByJobId(
                    analysisJob.getJobId()
                )
                .forEach(stat -> {

                    String key =
                        stat.getCoursePresentationId()
                        + "|"
                        + stat.getFinalResult();

                    existingKeys.add(key);
                });
        }
    }
}