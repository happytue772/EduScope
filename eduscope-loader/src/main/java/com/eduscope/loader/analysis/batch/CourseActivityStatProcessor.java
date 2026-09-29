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

import com.eduscope.loader.analysis.dto.CourseActivityStatCsvRow;
import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.entity.CourseActivityStat;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.analysis.repository.CourseActivityStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;

/**
 * course-activity TSV
 * → COURSE_ACTIVITY_STAT 변환.
 *
 * jobId가 전달되면 해당 ANALYSIS_JOB을 직접 사용하고,
 * 없으면 기존 최신 SUCCESS COURSE_ACTIVITY Job을 사용한다.
 */
@Component
@StepScope
public class CourseActivityStatProcessor
        implements ItemProcessor<
            CourseActivityStatCsvRow,
            CourseActivityStat> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final AnalysisJobRepository analysisJobRepository;
    private final CourseActivityStatRepository statRepository;
    private final CoursePresentationRepository courseRepository;

    /*
     * 명시적으로 사용할 ANALYSIS_JOB ID.
     * null이면 기존 방식으로 fallback.
     */
    private final Long requestedJobId;

    private Dataset dataset;
    private AnalysisJob analysisJob;

    /*
     * 강의 조회 반복을 줄이기 위한 Cache.
     */
    private final Map<String, CoursePresentation> courseCache =
            new HashMap<>();

    /*
     * 기존 DB 데이터 + 현재 TSV 내부 중복 방지.
     */
    private Set<Long> existingCourseIds;

    public CourseActivityStatProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            CourseActivityStatRepository statRepository,
            CoursePresentationRepository courseRepository,
            @Value("#{jobParameters['jobId']}")
            Long requestedJobId) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.analysisJobRepository = analysisJobRepository;
        this.statRepository = statRepository;
        this.courseRepository = courseRepository;
        this.requestedJobId = requestedJobId;
    }

    @Override
    public CourseActivityStat process(
            CourseActivityStatCsvRow item) {

        initialize();

        String courseKey =
            item.getCodeModule()
            + "|"
            + item.getCodePresentation();

        /*
         * CODE_MODULE + CODE_PRESENTATION
         * → 실제 COURSE_PRESENTATION_ID 변환.
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
                            "COURSE_PRESENTATION 없음"
                            + " / key="
                            + courseKey
                        )
                    )
            );

        Long courseId =
            course.getCoursePresentationId();

        /*
         * 기존 기능 유지.
         *
         * 이미 같은 Job에 저장된 Course 또는
         * TSV 내부 중복 Course면 Writer로 넘기지 않는다.
         */
        if (!existingCourseIds.add(
                courseId)) {

            return null;
        }

        CourseActivityStat stat =
            new CourseActivityStat();

        stat.setJobId(
            analysisJob.getJobId()
        );

        stat.setCoursePresentationId(
            courseId
        );

        stat.setActiveStudentCount(
            item.getActiveStudentCount()
        );

        stat.setTotalClickCount(
            item.getTotalClickCount()
        );

        stat.setAvgClickPerStudent(
            item.getAvgClickPerStudent()
        );

        stat.setActiveDayCount(
            item.getActiveDayCount()
        );

        stat.setCreatedAt(
            LocalDateTime.now()
        );

        return stat;
    }

    /**
     * Dataset / AnalysisJob / 기존 적재 데이터 초기화.
     */
    private void initialize() {

        /*
         * 신규 방식:
         * jobId가 있으면 정확한 Job을 직접 조회.
         *
         * 기존 방식:
         * jobId가 없으면 최신 SUCCESS Job 사용.
         */
        if (analysisJob == null) {

            if (requestedJobId != null) {

                analysisJob =
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
                 * 잘못된 분석 결과가 적재되지 않도록
                 * 분석 유형 검증.
                 */
                if (!"COURSE_ACTIVITY".equals(
                        analysisJob.getAnalysisType())) {

                    throw new IllegalStateException(
                        "COURSE_ACTIVITY 분석 Job이 아닙니다."
                        + " / jobId="
                        + analysisJob.getJobId()
                        + " / analysisType="
                        + analysisJob.getAnalysisType()
                    );
                }

                /*
                 * Hadoop 분석이 SUCCESS인 Job만 적재.
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
                 * 해당 Job의 Dataset을
                 * Course 매핑 기준으로 사용.
                 */
                dataset =
                    analysisJob.getDataset();

            } else {

                /*
                 * 기존 Dataset 조회 방식 유지.
                 */
                if (dataset == null) {

                    dataset =
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
                }

                /*
                 * 기존 최신 SUCCESS 방식 유지.
                 */
                analysisJob =
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
        }

        /*
         * 같은 Job의 기존 COURSE_PRESENTATION_ID 조회.
         * 재실행 중복 방지.
         */
        if (existingCourseIds == null) {

            existingCourseIds =
                new HashSet<>(
                    statRepository
                        .findCoursePresentationIdsByJobId(
                            analysisJob.getJobId()
                        )
                );
        }
    }
}