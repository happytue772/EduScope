package com.eduscope.loader.reference.batch;

import java.time.LocalDateTime;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.dto.CourseCsvRow;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;

/**
 * courses.csv DTO를 COURSE_PRESENTATION Entity로 변환한다.
 */
@Component
public class CoursePresentationProcessor
        implements ItemProcessor<CourseCsvRow, CoursePresentation> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;
    private final CoursePresentationRepository courseRepository;

    private Dataset dataset;

    public CoursePresentationProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            CoursePresentationRepository courseRepository) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.courseRepository = courseRepository;
    }

    @Override
    public CoursePresentation process(
            CourseCsvRow item) {

        validate(item);

        /*
         * 최초 1회만 현재 OULAD DATASET 조회.
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
         * 실제 UK_COURSE_PRESENT:
         * DATASET_ID + CODE_MODULE + CODE_PRESENTATION
         *
         * 재실행 시 중복 INSERT 방지.
         */
        boolean alreadyExists =
                courseRepository
                    .findByDatasetAndCodeModuleAndCodePresentation(
                            dataset,
                            item.getCodeModule(),
                            item.getCodePresentation()
                    )
                    .isPresent();

        if (alreadyExists) {
            return null;
        }

        CoursePresentation course =
                new CoursePresentation();

        course.setDataset(dataset);

        course.setCodeModule(
                item.getCodeModule()
        );

        course.setCodePresentation(
                item.getCodePresentation()
        );

        course.setModulePresentationLength(
                item.getModulePresentationLength()
        );

        course.setCreatedAt(
                LocalDateTime.now()
        );

        return course;
    }

    /**
     * Oracle NOT NULL 조건에 맞는 실제 값인지 검증.
     */
    private void validate(CourseCsvRow item) {

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

        if (item.getModulePresentationLength() == null) {

            throw new IllegalArgumentException(
                    "module_presentation_length 값이 없습니다."
            );
        }
    }
}