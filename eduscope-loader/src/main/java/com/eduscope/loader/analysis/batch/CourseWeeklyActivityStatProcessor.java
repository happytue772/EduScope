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

import com.eduscope.loader.analysis.dto.CourseWeeklyActivityStatCsvRow;
import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.entity.CourseWeeklyActivityStat;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.analysis.repository.CourseWeeklyActivityStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;

/**
 * course-weekly-activity.tsv 결과를
 * COURSE_WEEKLY_ACTIVITY_STAT으로 변환한다.
 */
@Component
@StepScope
public class CourseWeeklyActivityStatProcessor
        implements ItemProcessor<
            CourseWeeklyActivityStatCsvRow,
            CourseWeeklyActivityStat> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;
    private final AnalysisJobRepository analysisJobRepository;
    private final CourseWeeklyActivityStatRepository statRepository;
    private final CoursePresentationRepository courseRepository;

    // jobId 지정 시 정확한 ANALYSIS_JOB을 선택한다.
    private final Long requestedJobId;

    private Dataset dataset;
    private AnalysisJob analysisJob;

    private final Map<String, CoursePresentation> courseCache =
            new HashMap<>();

    private Set<String> existingKeys;

    public CourseWeeklyActivityStatProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            CourseWeeklyActivityStatRepository statRepository,
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
    public CourseWeeklyActivityStat process(
            CourseWeeklyActivityStatCsvRow item) {

        initialize();

        String courseKey =
                item.getCodeModule()
                + "|"
                + item.getCodePresentation();

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

        Long courseId =
                course.getCoursePresentationId();

        String statKey =
                courseId
                + "|"
                + item.getRelativeWeekNo();

        /*
         * 기존 DB 결과와 TSV 내부 중복 모두 방지.
         */
        if (!existingKeys.add(statKey)) {
            return null;
        }

        CourseWeeklyActivityStat stat =
                new CourseWeeklyActivityStat();

        stat.setJobId(
            analysisJob.getJobId()
        );

        stat.setCoursePresentationId(
            courseId
        );

        stat.setRelativeWeekNo(
            item.getRelativeWeekNo()
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

        stat.setActiveMaterialCount(
            item.getActiveMaterialCount()
        );

        stat.setCreatedAt(
            LocalDateTime.now()
        );

        return stat;
    }

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

                if (!"COURSE_WEEKLY_ACTIVITY".equals(analysisJob.getAnalysisType())) {
                    throw new IllegalStateException(
                        "COURSE_WEEKLY_ACTIVITY 분석 Job이 아닙니다."
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
                        "COURSE_WEEKLY_ACTIVITY",
                        "SUCCESS"
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "COURSE_WEEKLY_ACTIVITY ANALYSIS_JOB 없음"
                        )
                    );
            }
        }

        if (existingKeys == null) {

            existingKeys = new HashSet<>();

            statRepository
                .findAllByJobId(
                    analysisJob.getJobId()
                )
                .forEach(stat -> {

                    String key =
                        stat.getCoursePresentationId()
                        + "|"
                        + stat.getRelativeWeekNo();

                    existingKeys.add(key);
                });
        }
    }
}