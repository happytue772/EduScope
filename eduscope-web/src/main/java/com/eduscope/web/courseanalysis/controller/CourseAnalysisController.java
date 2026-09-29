package com.eduscope.web.courseanalysis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.courseanalysis.dto.CourseAnalysisResponse;
import com.eduscope.web.courseanalysis.service.CourseAnalysisService;

/**
 * 강의 종합 분석 REST API.
 */
@RestController
@RequestMapping("/api/course-analysis")
public class CourseAnalysisController {

    private final CourseAnalysisService service;

    public CourseAnalysisController(
            CourseAnalysisService service) {

        this.service = service;
    }

    @GetMapping("/{coursePresentationId}")
    public CourseAnalysisResponse getCourseAnalysis(
            @PathVariable Long coursePresentationId) {

        return service.getCourseAnalysis(
            coursePresentationId
        );
    }
}