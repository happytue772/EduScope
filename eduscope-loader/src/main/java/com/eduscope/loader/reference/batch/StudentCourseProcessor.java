package com.eduscope.loader.reference.batch;

import java.time.LocalDateTime;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.dto.StudentInfoCsvRow;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.entity.OuladStudent;
import com.eduscope.loader.reference.entity.StudentCourse;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;
import com.eduscope.loader.reference.repository.OuladStudentRepository;
import com.eduscope.loader.reference.repository.StudentCourseRepository;

/**
 * studentInfo.csv 한 행을 STUDENT_COURSE로 변환한다.
 */
@Component
public class StudentCourseProcessor
        implements ItemProcessor<StudentInfoCsvRow, StudentCourse> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final CoursePresentationRepository courseRepository;
    private final OuladStudentRepository studentRepository;
    private final StudentCourseRepository studentCourseRepository;

    private Dataset dataset;

    public StudentCourseProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            CoursePresentationRepository courseRepository,
            OuladStudentRepository studentRepository,
            StudentCourseRepository studentCourseRepository) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.studentCourseRepository = studentCourseRepository;
    }

    @Override
    public StudentCourse process(StudentInfoCsvRow item) {

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

        OuladStudent student =
                studentRepository
                    .findByDatasetAndSourceStudentId(
                            dataset,
                            item.getSourceStudentId()
                    )
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "OULAD_STUDENT FK 없음: "
                                    + item.getSourceStudentId()
                            )
                    );

        boolean alreadyExists =
                studentCourseRepository
                    .findByCoursePresentationAndStudent(
                            course,
                            student
                    )
                    .isPresent();

        if (alreadyExists) {
            return null;
        }

        StudentCourse studentCourse =
                new StudentCourse();

        studentCourse.setCoursePresentation(course);
        studentCourse.setStudent(student);

        studentCourse.setGender(
                emptyToNull(item.getGender())
        );

        studentCourse.setRegion(
                emptyToNull(item.getRegion())
        );

        studentCourse.setHighestEducation(
                emptyToNull(item.getHighestEducation())
        );

        studentCourse.setImdBand(
                emptyToNull(item.getImdBand())
        );

        studentCourse.setAgeBand(
                emptyToNull(item.getAgeBand())
        );

        studentCourse.setNumOfPrevAttempts(
                item.getNumOfPrevAttempts()
        );

        studentCourse.setStudiedCredits(
                item.getStudiedCredits()
        );

        studentCourse.setDisability(
                emptyToNull(item.getDisability())
        );

        studentCourse.setFinalResult(
                item.getFinalResult()
        );

        studentCourse.setCreatedAt(
                LocalDateTime.now()
        );

        return studentCourse;
    }

    private String emptyToNull(String value) {

        return value == null || value.isBlank()
                ? null
                : value;
    }
}