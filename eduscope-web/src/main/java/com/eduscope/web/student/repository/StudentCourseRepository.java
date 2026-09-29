package com.eduscope.web.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.student.entity.StudentCourse;

/**
 * STUDENT_COURSE 조회 Repository.
 */
public interface StudentCourseRepository
        extends JpaRepository<StudentCourse, Long> {

    /** 목록 DTO에서 사용하는 학생과 강의 정보를 함께 조회한다. */
    @Override
    @EntityGraph(attributePaths = {
        "student",
        "coursePresentation"
    })
    Page<StudentCourse> findAll(Pageable pageable);

    /**
     * 특정 강의의 수강학생 목록.
     */
    @EntityGraph(attributePaths = {
        "student",
        "coursePresentation"
    })
    Page<StudentCourse>
        findByCoursePresentation_CoursePresentationId(
            Long coursePresentationId,
            Pageable pageable
        );

    /**
     * 특정 학생의 수강정보.
     */
    @EntityGraph(attributePaths = {
        "student",
        "coursePresentation"
    })
    Page<StudentCourse>
        findByStudent_StudentId(
            Long studentId,
            Pageable pageable
        );
}
