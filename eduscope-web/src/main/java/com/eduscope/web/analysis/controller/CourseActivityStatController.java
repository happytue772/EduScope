package com.eduscope.web.analysis.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.CourseActivityStatResponse;
import com.eduscope.web.analysis.service.CourseActivityStatService;

/**
 * COURSE_ACTIVITY_STAT REST API.
 */
@RestController
@RequestMapping("/api/analysis/course-activity")
public class CourseActivityStatController {

    private final CourseActivityStatService service;

    public CourseActivityStatController(
            CourseActivityStatService service) {

        this.service = service;
    }

    @GetMapping
    public Page<CourseActivityStatResponse> getStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStats(page, size);
    }

    @GetMapping("/job/{jobId}")
    public Page<CourseActivityStatResponse> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByJob(jobId, page, size);
    }

    @GetMapping("/course/{coursePresentationId}")
    public Page<CourseActivityStatResponse> getByCourse(
            @PathVariable Long coursePresentationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByCourse(
            coursePresentationId,
            page,
            size
        );
    }
}