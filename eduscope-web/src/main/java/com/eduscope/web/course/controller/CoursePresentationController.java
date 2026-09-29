package com.eduscope.web.course.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.course.dto.CoursePresentationResponse;
import com.eduscope.web.course.service.CoursePresentationService;

/**
 * COURSE_PRESENTATION REST API.
 */
@RestController
@RequestMapping("/api/courses")
public class CoursePresentationController {

    private final CoursePresentationService courseService;

    public CoursePresentationController(
            CoursePresentationService courseService) {

        this.courseService = courseService;
    }

    /**
     * 전체 강의 조회.
     */
    @GetMapping
    public List<CoursePresentationResponse> getCourses() {

        return courseService.getCourses();
    }

    /**
     * 특정 강의 조회.
     */
    @GetMapping("/{coursePresentationId}")
    public CoursePresentationResponse getCourse(
            @PathVariable Long coursePresentationId) {

        return courseService.getCourse(coursePresentationId);
    }
}