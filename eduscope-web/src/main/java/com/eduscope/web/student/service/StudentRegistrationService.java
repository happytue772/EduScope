package com.eduscope.web.student.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.student.dto.StudentRegistrationResponse;
import com.eduscope.web.student.entity.StudentRegistration;
import com.eduscope.web.student.repository.StudentRegistrationRepository;

/**
 * 학생 등록정보 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class StudentRegistrationService {

    private final StudentRegistrationRepository repository;

    public StudentRegistrationService(
            StudentRegistrationRepository repository) {

        this.repository = repository;
    }

    /**
     * 등록정보 목록을 페이지 단위로 조회.
     */
    public Page<StudentRegistrationResponse> getRegistrations(
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
            .map(StudentRegistrationResponse::from);
    }

    /**
     * STUDENT_COURSE_ID 기준 단건 조회.
     */
    public StudentRegistrationResponse getRegistration(
            Long studentCourseId) {

        StudentRegistration registration =
            repository
                .findByStudentCourseId(studentCourseId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "STUDENT_REGISTRATION을 찾을 수 없습니다. "
                        + "studentCourseId="
                        + studentCourseId
                    )
                );

        return StudentRegistrationResponse.from(registration);
    }
}