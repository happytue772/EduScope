package com.eduscope.web.student.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.student.dto.StudentCourseResponse;
import com.eduscope.web.student.entity.StudentCourse;
import com.eduscope.web.student.repository.StudentCourseRepository;

/**
 * 학생 수강정보 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class StudentCourseService {

    private final StudentCourseRepository repository;

    public StudentCourseService(
            StudentCourseRepository repository) {

        this.repository = repository;
    }

    public Page<StudentCourseResponse> getStudentCourses(
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("studentCourseId").ascending()
            );

        return repository
            .findAll(pageable)
            .map(StudentCourseResponse::from);
    }

    public StudentCourseResponse getStudentCourse(
            Long studentCourseId) {

        StudentCourse course =
            repository
                .findById(studentCourseId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "STUDENT_COURSE를 찾을 수 없습니다. id="
                        + studentCourseId
                    )
                );

        return StudentCourseResponse.from(course);
    }
}