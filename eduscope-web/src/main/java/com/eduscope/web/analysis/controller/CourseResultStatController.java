package com.eduscope.web.analysis.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.CourseResultStatResponse;
import com.eduscope.web.analysis.service.CourseResultStatService;

/**
 * COURSE_RESULT_STAT REST API.
 */
@RestController
@RequestMapping("/api/analysis/course-result")
public class CourseResultStatController {

    private final CourseResultStatService service;

    public CourseResultStatController(
            CourseResultStatService service) {

        this.service = service;
    }

    @GetMapping
    public Page<CourseResultStatResponse> getStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStats(page, size);
    }

    @GetMapping("/job/{jobId}")
    public Page<CourseResultStatResponse> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByJob(jobId, page, size);
    }

    @GetMapping("/course/{coursePresentationId}")
    public Page<CourseResultStatResponse> getByCourse(
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