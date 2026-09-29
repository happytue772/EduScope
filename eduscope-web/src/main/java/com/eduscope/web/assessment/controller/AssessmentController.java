package com.eduscope.web.assessment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.assessment.dto.AssessmentResponse;
import com.eduscope.web.assessment.service.AssessmentService;

/**
 * ASSESSMENT REST API.
 */
@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService service;

    public AssessmentController(
            AssessmentService service) {

        this.service = service;
    }

    /**
     * 전체 평가 목록 조회.
     */
    @GetMapping
    public List<AssessmentResponse> getAssessments() {

        return service.getAssessments();
    }

    /**
     * ASSESSMENT_ID 기준 단건 조회.
     */
    @GetMapping("/{assessmentId}")
    public AssessmentResponse getAssessment(
            @PathVariable Long assessmentId) {

        return service.getAssessment(assessmentId);
    }

    /**
     * 특정 강의의 평가 목록 조회.
     *
     * 예:
     * /api/assessments/course?coursePresentationId=1
     */
    @GetMapping("/course")
    public List<AssessmentResponse> getAssessmentsByCourse(
            @RequestParam Long coursePresentationId) {

        return service.getAssessmentsByCourse(
            coursePresentationId
        );
    }
}