package com.eduscope.loader.reference.batch;

import java.time.LocalDateTime;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.dto.StudentRegistrationCsvRow;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.entity.OuladStudent;
import com.eduscope.loader.reference.entity.StudentCourse;
import com.eduscope.loader.reference.entity.StudentRegistration;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;
import com.eduscope.loader.reference.repository.OuladStudentRepository;
import com.eduscope.loader.reference.repository.StudentCourseRepository;
import com.eduscope.loader.reference.repository.StudentRegistrationRepository;

/**
 * studentRegistration.csv를 STUDENT_REGISTRATION Entity로 변환한다.
 */
@Component
public class StudentRegistrationProcessor
        implements ItemProcessor<
            StudentRegistrationCsvRow,
            StudentRegistration> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final CoursePresentationRepository courseRepository;
    private final OuladStudentRepository studentRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final StudentRegistrationRepository registrationRepository;

    private Dataset dataset;

    public StudentRegistrationProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            CoursePresentationRepository courseRepository,
            OuladStudentRepository studentRepository,
            StudentCourseRepository studentCourseRepository,
            StudentRegistrationRepository registrationRepository) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public StudentRegistration process(
            StudentRegistrationCsvRow item) {

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
         * code_module + code_presentation으로
         * COURSE_PRESENTATION FK 해결
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

        /*
         * 원본 id_student로 OULAD_STUDENT FK 해결
         */
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

        /*
         * 학생 + 강의 조합으로 STUDENT_COURSE 조회
         */
        StudentCourse studentCourse =
                studentCourseRepository
                    .findByCoursePresentationAndStudent(
                            course,
                            student
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "STUDENT_COURSE FK 없음: "
                            + item.getCodeModule()
                            + "|"
                            + item.getCodePresentation()
                            + "|"
                            + item.getSourceStudentId()
                        )
                    );

        /*
         * PK = STUDENT_COURSE_ID
         * 재실행 시 중복 INSERT 방지
         */
        if (registrationRepository.existsById(
                studentCourse.getStudentCourseId())) {

            return null;
        }

        StudentRegistration registration =
                new StudentRegistration();

        /*
         * @MapsId에 의해 STUDENT_COURSE_ID가
         * 그대로 PK + FK가 된다.
         */
        registration.setStudentCourseId(
                studentCourse.getStudentCourseId()
        );

        registration.setRegistrationDay(
                item.getRegistrationDay()
        );

        registration.setUnregistrationDay(
                item.getUnregistrationDay()
        );

        registration.setCreatedAt(
                LocalDateTime.now()
        );

        return registration;
    }
}