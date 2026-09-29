package com.eduscope.loader.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.loader.reference.entity.StudentAssessment;

/**
 * STUDENT_ASSESSMENT Repository.
 */
public interface StudentAssessmentRepository
        extends JpaRepository<StudentAssessment, Long> {

    /**
     * 실제 UK_ST_ASSESSMENT 기준 중복 검사.
     */
    boolean existsByStudentCourseIdAndAssessmentId(
            Long studentCourseId,
            Long assessmentId);
}