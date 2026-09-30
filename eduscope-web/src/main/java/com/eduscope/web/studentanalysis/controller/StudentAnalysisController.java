package com.eduscope.web.studentanalysis.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.studentanalysis.dto.StudentAnalysisResponse;
import com.eduscope.web.studentanalysis.dto.StudentSearchResponse;
import com.eduscope.web.studentanalysis.service.StudentAnalysisService;

/**
 * 학생 분석 REST API.
 */
@RestController
@RequestMapping("/api/student-analysis")
public class StudentAnalysisController {

    private final StudentAnalysisService service;

    public StudentAnalysisController(
            StudentAnalysisService service) {

        this.service = service;
    }


    /**
     * 학생 수강 정보 검색.
     */
    @GetMapping("/search")
    public List<StudentSearchResponse> search(
            @RequestParam(
                required = false,
                defaultValue = ""
            )
            String keyword,

            @RequestParam(
                required = false
            )
            Long coursePresentationId,

            @RequestParam(
                required = false,
                defaultValue = "0"
            )
            Integer offset,

            @RequestParam(
                required = false,
                defaultValue = "100"
            )
            Integer limit) {

        return service.search(
            keyword,
            coursePresentationId,
            offset,
            limit
        );
    }


    /**
     * 특정 학생-강의 상세 분석.
     */
    @GetMapping("/{studentCourseId}")
    public StudentAnalysisResponse getAnalysis(
            @PathVariable Long studentCourseId) {

        return service.getAnalysis(
            studentCourseId
        );
    }
}