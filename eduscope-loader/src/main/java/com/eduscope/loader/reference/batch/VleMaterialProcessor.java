package com.eduscope.loader.reference.batch;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.dto.VleMaterialCsvRow;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.entity.VleMaterial;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;
import com.eduscope.loader.reference.repository.VleMaterialRepository;

/**
 * vle.csv 데이터를 VLE_MATERIAL Entity로 변환한다.
 */
@Component
@StepScope
public class VleMaterialProcessor
        implements ItemProcessor<VleMaterialCsvRow, VleMaterial> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;
    private final CoursePresentationRepository courseRepository;
    private final VleMaterialRepository vleMaterialRepository;

    private Dataset dataset;

    /*
     * 동일 강의를 반복 조회하지 않기 위한 캐시.
     */
    private final Map<String, CoursePresentation> courseCache =
            new HashMap<>();

    /*
     * 동일 Job 안에서 같은 VLE 자료가 중복될 경우 방지.
     */
    private final Set<String> seenKeys =
            new HashSet<>();

    public VleMaterialProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            CoursePresentationRepository courseRepository,
            VleMaterialRepository vleMaterialRepository) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.courseRepository = courseRepository;
        this.vleMaterialRepository = vleMaterialRepository;
    }

    @Override
    public VleMaterial process(VleMaterialCsvRow item) {

        validate(item);

        Dataset currentDataset = getDataset();

        String courseKey =
                item.getCodeModule()
                + "|"
                + item.getCodePresentation();

        CoursePresentation course =
                courseCache.computeIfAbsent(
                    courseKey,
                    key -> courseRepository
                        .findByDatasetAndCodeModuleAndCodePresentation(
                            currentDataset,
                            item.getCodeModule(),
                            item.getCodePresentation()
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "COURSE_PRESENTATION을 찾을 수 없습니다: "
                                + courseKey
                            )
                        )
                );

        /*
         * 실제 UNIQUE 기준.
         */
        String uniqueKey =
                currentDataset.getDatasetId()
                + "|"
                + courseKey
                + "|"
                + item.getSourceSiteId();

        // 현재 Job 내부 중복
        if (!seenKeys.add(uniqueKey)) {
            return null;
        }

        // 기존 Oracle 데이터 중복
        if (vleMaterialRepository
                .findByDatasetAndCoursePresentationAndSourceSiteId(
                    currentDataset,
                    course,
                    item.getSourceSiteId()
                )
                .isPresent()) {

            return null;
        }

        VleMaterial material =
                new VleMaterial();

        material.setDataset(currentDataset);
        material.setCoursePresentation(course);

        material.setSourceSiteId(
            item.getSourceSiteId()
        );

        material.setActivityType(
            item.getActivityType()
        );

        /*
         * 원본 공백은 null 그대로 보존.
         */
        material.setWeekFrom(
            item.getWeekFrom()
        );

        material.setWeekTo(
            item.getWeekTo()
        );

        material.setCreatedAt(
            LocalDateTime.now()
        );

        return material;
    }

    private Dataset getDataset() {

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

        return dataset;
    }

    private void validate(VleMaterialCsvRow item) {

        if (item.getSourceSiteId() == null) {
            throw new IllegalArgumentException(
                "id_site 값이 없습니다."
            );
        }

        if (item.getCodeModule() == null
                || item.getCodeModule().isBlank()) {

            throw new IllegalArgumentException(
                "code_module 값이 없습니다."
            );
        }

        if (item.getCodePresentation() == null
                || item.getCodePresentation().isBlank()) {

            throw new IllegalArgumentException(
                "code_presentation 값이 없습니다."
            );
        }

        if (item.getActivityType() == null
                || item.getActivityType().isBlank()) {

            throw new IllegalArgumentException(
                "activity_type 값이 없습니다."
            );
        }
    }
}