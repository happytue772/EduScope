package com.eduscope.loader.reference.batch;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.dto.AssessmentCsvRow;
import com.eduscope.loader.reference.entity.Assessment;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.repository.AssessmentRepository;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;

/**
 * assessments.csv 데이터를 ASSESSMENT Entity로 변환한다.
 */
@Component
@StepScope
public class AssessmentProcessor
        implements ItemProcessor<AssessmentCsvRow, Assessment> {

    private static final Set<String> VALID_TYPES =
            Set.of("TMA", "CMA", "Exam");

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;
    private final CoursePresentationRepository courseRepository;
    private final AssessmentRepository assessmentRepository;

    private Dataset dataset;

    public AssessmentProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            CoursePresentationRepository courseRepository,
            AssessmentRepository assessmentRepository) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.courseRepository = courseRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @Override
    public Assessment process(AssessmentCsvRow item) {

        validate(item);

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
         * 실제 UNIQUE 기준으로 재실행 중복 방지.
         */
        if (assessmentRepository
                .findByDatasetAndSourceAssessmentId(
                    dataset,
                    item.getSourceAssessmentId()
                )
                .isPresent()) {

            return null;
        }

        /*
         * code_module + code_presentation으로
         * COURSE_PRESENTATION FK 조회.
         */
        CoursePresentation course =
                courseRepository
                    .findByDatasetAndCodeModuleAndCodePresentation(
                        dataset,
                        item.getCodeModule(),
                        item.getCodePresentation()
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "COURSE_PRESENTATION FK 없음: "
                            + item.getCodeModule()
                            + "|"
                            + item.getCodePresentation()
                        )
                    );

        Assessment assessment =
                new Assessment();

        assessment.setDataset(dataset);
        assessment.setCoursePresentation(course);

        assessment.setSourceAssessmentId(
            item.getSourceAssessmentId()
        );

        assessment.setAssessmentType(
            item.getAssessmentType()
        );

        /*
         * OULAD date는 실제 날짜가 아니라
         * 강의 시작일 기준 상대 일수.
         * 빈 값은 null 그대로 저장.
         */
        assessment.setAssessmentDueDay(
            item.getAssessmentDueDay()
        );

        assessment.setAssessmentWeight(
            item.getAssessmentWeight()
        );

        assessment.setCreatedAt(
            LocalDateTime.now()
        );

        return assessment;
    }

    private void validate(AssessmentCsvRow item) {

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

        if (item.getSourceAssessmentId() == null) {

            throw new IllegalArgumentException(
                "id_assessment 값이 없습니다."
            );
        }

        /*
         * 실제 Oracle CK_ASSESS_TYPE과 동일.
         */
        if (!VALID_TYPES.contains(
                item.getAssessmentType())) {

            throw new IllegalArgumentException(
                "허용되지 않은 assessment_type: "
                + item.getAssessmentType()
            );
        }

        if (item.getAssessmentWeight() == null) {

            throw new IllegalArgumentException(
                "weight 값이 없습니다."
            );
        }
    }
}