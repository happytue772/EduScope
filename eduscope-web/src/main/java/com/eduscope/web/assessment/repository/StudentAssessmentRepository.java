package com.eduscope.web.assessment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.assessment.entity.StudentAssessment;

/**
 * STUDENT_ASSESSMENT 조회 Repository.
 */
public interface StudentAssessmentRepository
        extends JpaRepository<StudentAssessment, Long> {

    /** DTO 변환에 필요한 학생·강의·평가를 목록 조회에 포함한다. */
    @Override
    @EntityGraph(attributePaths = {
        "studentCourse.student",
        "studentCourse.coursePresentation",
        "assessment"
    })
    Page<StudentAssessment> findAll(Pageable pageable);

    /**
     * 특정 수강 건의 평가 결과 조회.
     */
    @EntityGraph(attributePaths = {
        "studentCourse.student",
        "studentCourse.coursePresentation",
        "assessment"
    })
    Page<StudentAssessment>
        findByStudentCourse_StudentCourseId(
            Long studentCourseId,
            Pageable pageable
        );

    /**
     * 특정 평가의 학생 제출 결과 조회.
     */
    @EntityGraph(attributePaths = {
        "studentCourse.student",
        "studentCourse.coursePresentation",
        "assessment"
    })
    Page<StudentAssessment>
        findByAssessment_AssessmentId(
            Long assessmentId,
            Pageable pageable
        );
}
