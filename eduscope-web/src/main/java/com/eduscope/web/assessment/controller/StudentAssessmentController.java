package com.eduscope.web.assessment.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.assessment.dto.StudentAssessmentResponse;
import com.eduscope.web.assessment.service.StudentAssessmentService;

/**
 * STUDENT_ASSESSMENT REST API.
 */
@RestController
@RequestMapping("/api/student-assessments")
public class StudentAssessmentController {

    private final StudentAssessmentService service;

    public StudentAssessmentController(
            StudentAssessmentService service) {

        this.service = service;
    }

    /**
     * 전체 학생 평가결과.
     */
    @GetMapping
    public Page<StudentAssessmentResponse> getStudentAssessments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getStudentAssessments(page, size);
    }

    /**
     * 학생 평가결과 단건 조회.
     */
    @GetMapping("/{studentAssessmentId}")
    public StudentAssessmentResponse getStudentAssessment(
            @PathVariable Long studentAssessmentId) {

        return service.getStudentAssessment(
            studentAssessmentId
        );
    }

    /**
     * 특정 수강 학생의 평가결과.
     */
    @GetMapping("/student-course/{studentCourseId}")
    public Page<StudentAssessmentResponse>
            getByStudentCourse(
                @PathVariable Long studentCourseId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size) {

        return service.getByStudentCourse(
            studentCourseId,
            page,
            size
        );
    }

    /**
     * 특정 평가의 학생 결과.
     */
    @GetMapping("/assessment/{assessmentId}")
    public Page<StudentAssessmentResponse>
            getByAssessment(
                @PathVariable Long assessmentId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size) {

        return service.getByAssessment(
            assessmentId,
            page,
            size
        );
    }
}