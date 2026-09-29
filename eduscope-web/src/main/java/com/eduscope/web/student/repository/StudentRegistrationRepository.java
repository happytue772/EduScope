package com.eduscope.web.student.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.student.entity.StudentRegistration;

/**
 * STUDENT_REGISTRATION 조회 Repository.
 */
public interface StudentRegistrationRepository
        extends JpaRepository<StudentRegistration, Long> {

    /** 등록 목록 DTO의 수강·학생·강의 정보를 함께 조회한다. */
    @Override
    @EntityGraph(attributePaths = {
        "studentCourse.student",
        "studentCourse.coursePresentation"
    })
    Page<StudentRegistration> findAll(Pageable pageable);

    /**
     * STUDENT_COURSE_ID 기준 등록정보 조회.
     */
    Optional<StudentRegistration>
        findByStudentCourseId(Long studentCourseId);
}
