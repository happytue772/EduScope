package com.eduscope.web.student.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.student.dto.StudentCourseResponse;
import com.eduscope.web.student.service.StudentCourseService;

/**
 * STUDENT_COURSE REST API.
 */
@RestController
@RequestMapping("/api/student-courses")
public class StudentCourseController {

    private final StudentCourseService service;

    public StudentCourseController(
            StudentCourseService service) {

        this.service = service;
    }

    @GetMapping
    public Page<StudentCourseResponse> getStudentCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStudentCourses(page, size);
    }

    @GetMapping("/{studentCourseId}")
    public StudentCourseResponse getStudentCourse(
            @PathVariable Long studentCourseId) {

        return service.getStudentCourse(studentCourseId);
    }
}