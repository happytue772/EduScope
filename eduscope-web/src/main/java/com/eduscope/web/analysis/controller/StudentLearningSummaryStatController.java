package com.eduscope.web.analysis.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.StudentLearningSummaryStatResponse;
import com.eduscope.web.analysis.service.StudentLearningSummaryStatService;

/**
 * STUDENT_LEARNING_SUMMARY_STAT REST API.
 */
@RestController
@RequestMapping("/api/analysis/student-learning-summary")
public class StudentLearningSummaryStatController {

    private final StudentLearningSummaryStatService service;

    public StudentLearningSummaryStatController(
            StudentLearningSummaryStatService service) {

        this.service = service;
    }

    @GetMapping
    public Page<StudentLearningSummaryStatResponse> getStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStats(page, size);
    }

    @GetMapping("/job/{jobId}")
    public Page<StudentLearningSummaryStatResponse> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByJob(jobId, page, size);
    }

    @GetMapping("/student-course/{studentCourseId}")
    public Page<StudentLearningSummaryStatResponse> getByStudentCourse(
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