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

import com.eduscope.loader.analysis.dto.RegistrationStatCsvRow;
import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.entity.RegistrationStat;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.analysis.repository.RegistrationStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;

/**
 * registration.tsv → REGISTRATION_STAT 변환.
 */
@Component
@StepScope
public class RegistrationStatProcessor
        implements ItemProcessor<
            RegistrationStatCsvRow,
            RegistrationStat> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;
    private final AnalysisJobRepository analysisJobRepository;
    private final RegistrationStatRepository statRepository;
    private final CoursePresentationRepository courseRepository;

    // jobId 지정 시 정확한 ANALYSIS_JOB을 선택한다.
    private final Long requestedJobId;

    private Dataset dataset;
    private AnalysisJob analysisJob;

    private final Map<String, CoursePresentation> courseCache =
            new HashMap<>();

    private Set<Long> existingCourseIds;

    public RegistrationStatProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            RegistrationStatRepository statRepository,
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
    public RegistrationStat process(
            RegistrationStatCsvRow item) {

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

        // 기존 DB 및 TSV 내부 중복 방지
        if (!existingCourseIds.add(courseId)) {
            return null;
        }

        RegistrationStat stat =
                new RegistrationStat();

        stat.setJobId(
            analysisJob.getJobId()
        );

        stat.setCoursePresentationId(courseId);

        stat.setRegistrationCount(
            item.getRegistrationCount()
        );

        stat.setUnregistrationCount(
            item.getUnregistrationCount()
        );

        stat.setWithdrawnResultCount(
            item.getWithdrawnResultCount()
        );

        // MapReduce 비율값 그대로 보존
        stat.setUnregistrationRate(
            item.getUnregistrationRate()
        );

        stat.setAvgRegistrationDay(
            item.getAvgRegistrationDay()
        );

        stat.setAvgUnregistrationDay(
            item.getAvgUnregistrationDay()
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

                if (!"REGISTRATION".equals(analysisJob.getAnalysisType())) {
                    throw new IllegalStateException(
                        "REGISTRATION 분석 Job이 아닙니다."
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
                        "REGISTRATION",
                        "SUCCESS"
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "REGISTRATION ANALYSIS_JOB 없음"
                        )
                    );
            }
        }

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