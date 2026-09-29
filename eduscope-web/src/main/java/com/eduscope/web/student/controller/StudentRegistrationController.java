package com.eduscope.web.student.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.student.dto.StudentRegistrationResponse;
import com.eduscope.web.student.service.StudentRegistrationService;

/**
 * 학생 등록정보 REST API.
 */
@RestController
@RequestMapping("/api/student-registrations")
public class StudentRegistrationController {

    private final StudentRegistrationService service;

    public StudentRegistrationController(
            StudentRegistrationService service) {

        this.service = service;
    }

    /**
     * 등록정보 목록 조회.
     */
    @GetMapping
    public Page<StudentRegistrationResponse> getRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getRegistrations(page, size);
    }

    /**
     * STUDENT_COURSE_ID 기준 조회.
     */
    @GetMapping("/{studentCourseId}")
    public StudentRegistrationResponse getRegistration(
            @PathVariable Long studentCourseId) {

        return service.getRegistration(studentCourseId);
    }
}