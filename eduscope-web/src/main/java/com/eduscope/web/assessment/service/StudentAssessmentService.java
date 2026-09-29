package com.eduscope.web.assessment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.assessment.dto.StudentAssessmentResponse;
import com.eduscope.web.assessment.entity.StudentAssessment;
import com.eduscope.web.assessment.repository.StudentAssessmentRepository;

/**
 * 학생별 평가 결과 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class StudentAssessmentService {

    private final StudentAssessmentRepository repository;

    public StudentAssessmentService(
            StudentAssessmentRepository repository) {

        this.repository = repository;
    }

    /**
     * 전체 평가결과 페이지 조회.
     */
    public Page<StudentAssessmentResponse> getStudentAssessments(
            int page,
            int size) {

        Pageable pageable =
            createPageable(page, size);

        return repository
            .findAll(pageable)
            .map(StudentAssessmentResponse::from);
    }

    /**
     * 학생 평가 결과 단건 조회.
     */
    public StudentAssessmentResponse getStudentAssessment(
            Long studentAssessmentId) {

        StudentAssessment result =
            repository
                .findById(studentAssessmentId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "STUDENT_ASSESSMENT를 찾을 수 없습니다. id="
                        + studentAssessmentId
                    )
                );

        return StudentAssessmentResponse.from(result);
    }

    /**
     * 특정 수강 학생의 평가 결과.
     */
    public Page<StudentAssessmentResponse>
            getByStudentCourse(
                Long studentCourseId,
                int page,
                int size) {

        return repository
            .findByStudentCourse_StudentCourseId(
                studentCourseId,
                createPageable(page, size)
            )
            .map(StudentAssessmentResponse::from);
    }

    /**
     * 특정 평가의 모든 학생 결과.
     */
    public Page<StudentAssessmentResponse>
            getByAssessment(
                Long assessmentId,
                int page,
                int size) {

        return repository
            .findByAssessment_AssessmentId(
                assessmentId,
                createPageable(page, size)
            )
            .map(StudentAssessmentResponse::from);
    }

    /**
     * 공통 페이지 설정.
     */
    private Pageable createPageable(
            int page,
            int size) {

        return PageRequest.of(
            page,
            size,
            Sort.by("studentAssessmentId").ascending()
        );
    }
}