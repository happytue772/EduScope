package com.eduscope.loader.reference.batch;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.dto.StudentAssessmentCsvRow;
import com.eduscope.loader.reference.entity.Assessment;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.entity.OuladStudent;
import com.eduscope.loader.reference.entity.StudentAssessment;
import com.eduscope.loader.reference.entity.StudentCourse;
import com.eduscope.loader.reference.repository.AssessmentRepository;
import com.eduscope.loader.reference.repository.OuladStudentRepository;
import com.eduscope.loader.reference.repository.StudentAssessmentRepository;
import com.eduscope.loader.reference.repository.StudentCourseRepository;

/**
 * studentAssessment.csv 한 행을
 * STUDENT_ASSESSMENT Entity로 변환한다.
 */
@Component
@StepScope
public class StudentAssessmentProcessor
        implements ItemProcessor<
            StudentAssessmentCsvRow,
            StudentAssessment> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final AssessmentRepository assessmentRepository;
    private final OuladStudentRepository studentRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final StudentAssessmentRepository studentAssessmentRepository;

    private Dataset dataset;

    /*
     * 동일 평가/학생을 반복 조회하지 않도록 캐시.
     */
    private final Map<Long, Assessment> assessmentCache =
            new HashMap<>();

    private final Map<Long, OuladStudent> studentCache =
            new HashMap<>();

    private final Map<String, StudentCourse> studentCourseCache =
            new HashMap<>();

    public StudentAssessmentProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AssessmentRepository assessmentRepository,
            OuladStudentRepository studentRepository,
            StudentCourseRepository studentCourseRepository,
            StudentAssessmentRepository studentAssessmentRepository) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.assessmentRepository = assessmentRepository;
        this.studentRepository = studentRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.studentAssessmentRepository =
                studentAssessmentRepository;
    }

    @Override
    public StudentAssessment process(
            StudentAssessmentCsvRow item) {

        validate(item);

        Dataset currentDataset = getDataset();

        /*
         * 원본 id_assessment → ASSESSMENT
         */
        Assessment assessment =
                assessmentCache.computeIfAbsent(
                    item.getSourceAssessmentId(),
                    id -> assessmentRepository
                        .findByDatasetAndSourceAssessmentId(
                            currentDataset,
                            id
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "ASSESSMENT를 찾을 수 없습니다: "
                                + id
                            )
                        )
                );

        /*
         * 원본 id_student → OULAD_STUDENT
         */
        OuladStudent student =
                studentCache.computeIfAbsent(
                    item.getSourceStudentId(),
                    id -> studentRepository
                        .findByDatasetAndSourceStudentId(
                            currentDataset,
                            id
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "OULAD_STUDENT를 찾을 수 없습니다: "
                                + id
                            )
                        )
                );

        /*
         * 해당 Assessment가 속한 실제 강의를 이용한다.
         */
        CoursePresentation course =
                assessment.getCoursePresentation();

        /*
         * 강의 + 학생 조합 → STUDENT_COURSE
         */
        String studentCourseKey =
                course.getCoursePresentationId()
                + "|"
                + student.getStudentId();

        StudentCourse studentCourse =
                studentCourseCache.computeIfAbsent(
                    studentCourseKey,
                    key -> studentCourseRepository
                        .findByCoursePresentationAndStudent(
                            course,
                            student
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "STUDENT_COURSE를 찾을 수 없습니다: "
                                + item.getSourceAssessmentId()
                                + "|"
                                + item.getSourceStudentId()
                            )
                        )
                );

        /*
         * 실제 UNIQUE 기준 재실행 중복 방지.
         */
        if (studentAssessmentRepository
                .existsByStudentCourseIdAndAssessmentId(
                    studentCourse.getStudentCourseId(),
                    assessment.getAssessmentId()
                )) {

            return null;
        }

        StudentAssessment result =
                new StudentAssessment();

        result.setStudentCourseId(
            studentCourse.getStudentCourseId()
        );

        result.setAssessmentId(
            assessment.getAssessmentId()
        );

        result.setSubmittedDay(
            item.getSubmittedDay()
        );

        /*
         * OULAD 0/1 → DB CHECK Y/N
         */
        result.setIsBanked(
            convertBanked(item.getBankedValue())
        );

        /*
         * SCORE는 실제 DB에서 NULL 허용.
         */
        result.setScore(
            item.getScore()
        );

        result.setCreatedAt(
            LocalDateTime.now()
        );

        return result;
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

    private void validate(
            StudentAssessmentCsvRow item) {

        if (item.getSourceAssessmentId() == null) {
            throw new IllegalArgumentException(
                "id_assessment 값이 없습니다."
            );
        }

        if (item.getSourceStudentId() == null) {
            throw new IllegalArgumentException(
                "id_student 값이 없습니다."
            );
        }

        if (item.getSubmittedDay() == null) {
            throw new IllegalArgumentException(
                "date_submitted 값이 없습니다."
            );
        }

        if (item.getBankedValue() == null) {
            throw new IllegalArgumentException(
                "is_banked 값이 없습니다."
            );
        }
    }

    /**
     * 실제 CK_ST_ASSESS_BANKED 기준으로 변환.
     */
    private String convertBanked(Integer value) {

        if (value == 0) {
            return "N";
        }

        if (value == 1) {
            return "Y";
        }

        throw new IllegalArgumentException(
            "허용되지 않은 is_banked 값: "
            + value
        );
    }
}