package com.eduscope.web.student.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.student.dto.OuladStudentResponse;
import com.eduscope.web.student.service.OuladStudentService;

/**
 * OULAD 학생 REST API.
 */
@RestController
@RequestMapping("/api/students")
public class OuladStudentController {

    private final OuladStudentService studentService;

    public OuladStudentController(
            OuladStudentService studentService) {

        this.studentService = studentService;
    }

    /**
     * 학생 목록 페이지 조회.
     */
    @GetMapping
    public Page<OuladStudentResponse> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return studentService.getStudents(page, size);
    }

    /**
     * 학생 단건 조회.
     */
    @GetMapping("/{studentId}")
    public OuladStudentResponse getStudent(
            @PathVariable Long studentId) {

        return studentService.getStudent(studentId);
    }
}