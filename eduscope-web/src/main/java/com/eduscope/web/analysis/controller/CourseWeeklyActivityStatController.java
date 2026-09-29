package com.eduscope.web.analysis.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.analysis.dto.CourseWeeklyActivityStatResponse;
import com.eduscope.web.analysis.service.CourseWeeklyActivityStatService;

/**
 * COURSE_WEEKLY_ACTIVITY_STAT REST API.
 */
@RestController
@RequestMapping("/api/analysis/course-weekly-activity")
public class CourseWeeklyActivityStatController {

    private final CourseWeeklyActivityStatService service;

    public CourseWeeklyActivityStatController(
            CourseWeeklyActivityStatService service) {

        this.service = service;
    }

    @GetMapping
    public Page<CourseWeeklyActivityStatResponse> getStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStats(page, size);
    }

    @GetMapping("/job/{jobId}")
    public Page<CourseWeeklyActivityStatResponse> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByJob(jobId, page, size);
    }

    @GetMapping("/course/{coursePresentationId}")
    public Page<CourseWeeklyActivityStatResponse> getByCourse(
            @PathVariable Long coursePresentationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByCourse(
            coursePresentationId,
            page,
            size
        );
    }

    @GetMapping("/course/{coursePresentationId}/week/{relativeWeekNo}")
    public Page<CourseWeeklyActivityStatResponse> getByCourseAndWeek(
            @PathVariable Long coursePresentationId,
            @PathVariable Integer relativeWeekNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getByCourseAndWeek(
            coursePresentationId,
            relativeWeekNo,
            page,
            size
        );
    }
}