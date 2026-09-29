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

import com.eduscope.loader.analysis.dto.CourseResultStatCsvRow;
import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.entity.CourseResultStat;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.analysis.repository.CourseResultStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;

@Component
@StepScope
public class CourseResultStatProcessor
        implements ItemProcessor<CourseResultStatCsvRow, CourseResultStat> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;
    private final AnalysisJobRepository jobRepository;
    private final CourseResultStatRepository statRepository;
    private final CoursePresentationRepository courseRepository;

    // jobId 지정 시 정확한 ANALYSIS_JOB을 선택한다.
    private final Long requestedJobId;

    private Dataset dataset;
    private AnalysisJob job;

    private final Map<String, CoursePresentation> courseCache =
            new HashMap<>();

    private Set<Long> existingIds;

    public CourseResultStatProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository jobRepository,
            CourseResultStatRepository statRepository,
            CoursePresentationRepository courseRepository,
            @Value("#{jobParameters['jobId']}")
            Long requestedJobId
) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.jobRepository = jobRepository;
        this.statRepository = statRepository;
        this.courseRepository = courseRepository;
        this.requestedJobId = requestedJobId;
    }

    @Override
    public CourseResultStat process(CourseResultStatCsvRow item) {

        initialize();

        String key =
                item.getCodeModule() + "|"
                + item.getCodePresentation();

        CoursePresentation course =
            courseCache.computeIfAbsent(
                key,
                k -> courseRepository
                    .findByDatasetAndCodeModuleAndCodePresentation(
                        dataset,
                        item.getCodeModule(),
                        item.getCodePresentation()
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "COURSE_PRESENTATION 없음: " + key
                        )
                    )
            );

        Long courseId = course.getCoursePresentationId();

        if (!existingIds.add(courseId)) {
            return null;
        }

        CourseResultStat stat = new CourseResultStat();

        stat.setJobId(job.getJobId());
        stat.setCoursePresentationId(courseId);

        stat.setStudentCount(item.getStudentCount());
        stat.setPassCount(item.getPassCount());
        stat.setFailCount(item.getFailCount());
        stat.setWithdrawnCount(item.getWithdrawnCount());
        stat.setDistinctionCount(item.getDistinctionCount());

        stat.setPassRate(item.getPassRate());
        stat.setFailRate(item.getFailRate());
        stat.setWithdrawnRate(item.getWithdrawnRate());
        stat.setDistinctionRate(item.getDistinctionRate());

        stat.setCreatedAt(LocalDateTime.now());

        return stat;
    }

    private void initialize() {

        if (job == null) {

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

                dataset = job.getDataset();

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
        }

        if (existingIds == null) {
            existingIds = new HashSet<>(
                statRepository.findCourseIdsByJobId(
                    job.getJobId()
                )
            );
        }
    }
}