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

import com.eduscope.loader.analysis.dto.VleActivityStatCsvRow;
import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.entity.VleActivityStat;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.analysis.repository.VleActivityStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.entity.VleMaterial;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;
import com.eduscope.loader.reference.repository.VleMaterialRepository;

/**
 * vle-activity.tsv의 SOURCE_SITE_ID를
 * 실제 VLE_MATERIAL_ID로 변환하여 통계 Entity 생성.
 */
@Component
@StepScope
public class VleActivityStatProcessor
        implements ItemProcessor<
            VleActivityStatCsvRow,
            VleActivityStat> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;
    private final AnalysisJobRepository analysisJobRepository;
    private final VleActivityStatRepository statRepository;
    private final CoursePresentationRepository courseRepository;
    private final VleMaterialRepository vleMaterialRepository;

    // jobId 지정 시 정확한 ANALYSIS_JOB을 선택한다.
    private final Long requestedJobId;

    private Dataset dataset;
    private AnalysisJob analysisJob;

    private final Map<String, CoursePresentation> courseCache =
            new HashMap<>();

    private final Map<String, VleMaterial> vleMaterialCache =
            new HashMap<>();

    private Set<Long> existingVleMaterialIds;

    public VleActivityStatProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            VleActivityStatRepository statRepository,
            CoursePresentationRepository courseRepository,
            VleMaterialRepository vleMaterialRepository,
            @Value("#{jobParameters['jobId']}")
            Long requestedJobId
) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.analysisJobRepository = analysisJobRepository;
        this.statRepository = statRepository;
        this.courseRepository = courseRepository;
        this.vleMaterialRepository = vleMaterialRepository;
        this.requestedJobId = requestedJobId;
    }

    @Override
    public VleActivityStat process(
            VleActivityStatCsvRow item) {

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

        String materialKey =
                course.getCoursePresentationId()
                + "|"
                + item.getSourceSiteId();

        VleMaterial vleMaterial =
                vleMaterialCache.computeIfAbsent(
                    materialKey,
                    key -> vleMaterialRepository
                        .findByDatasetAndCoursePresentationAndSourceSiteId(
                            dataset,
                            course,
                            item.getSourceSiteId()
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "VLE_MATERIAL 없음: "
                                + courseKey
                                + "|"
                                + item.getSourceSiteId()
                            )
                        )
                );

        Long vleMaterialId =
                vleMaterial.getVleMaterialId();

        // 기존 DB 결과 및 TSV 내부 중복 방지
        if (!existingVleMaterialIds.add(
                vleMaterialId)) {

            return null;
        }

        VleActivityStat stat =
                new VleActivityStat();

        stat.setJobId(
            analysisJob.getJobId()
        );

        stat.setVleMaterialId(
            vleMaterialId
        );

        stat.setTotalClickCount(
            item.getTotalClickCount()
        );

        stat.setActiveStudentCount(
            item.getActiveStudentCount()
        );

        stat.setActiveDayCount(
            item.getActiveDayCount()
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

                if (!"VLE_ACTIVITY".equals(analysisJob.getAnalysisType())) {
                    throw new IllegalStateException(
                        "VLE_ACTIVITY 분석 Job이 아닙니다."
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
                        "VLE_ACTIVITY",
                        "SUCCESS"
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "VLE_ACTIVITY ANALYSIS_JOB 없음"
                        )
                    );
            }
        }

        if (existingVleMaterialIds == null) {

            existingVleMaterialIds =
                new HashSet<>(
                    statRepository
                        .findVleMaterialIdsByJobId(
                            analysisJob.getJobId()
                        )
                );
        }
    }
}