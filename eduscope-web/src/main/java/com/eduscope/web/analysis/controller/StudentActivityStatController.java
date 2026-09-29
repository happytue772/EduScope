package com.eduscope.web.analysis.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.StudentActivityStatResponse;
import com.eduscope.web.analysis.service.StudentActivityStatService;

/**
 * STUDENT_ACTIVITY_STAT REST API.
 */
@RestController
@RequestMapping("/api/analysis/student-activity")
public class StudentActivityStatController {

    private final StudentActivityStatService service;

    public StudentActivityStatController(
            StudentActivityStatService service) {

        this.service = service;
    }

    @GetMapping
    public Page<StudentActivityStatResponse> getStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStats(page, size);
    }

    @GetMapping("/job/{jobId}")
    public Page<StudentActivityStatResponse> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByJob(jobId, page, size);
    }

    @GetMapping("/student-course/{studentCourseId}")
    public Page<StudentActivityStatResponse> getByStudentCourse(
            @PathVariable Long studentCourseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByStudentCourse(
            studentCourseId,
            page,
            size
        );
    }
}