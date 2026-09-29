package com.eduscope.web.analysis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.StudentActivityStatResponse;
import com.eduscope.web.analysis.repository.StudentActivityStatRepository;

/**
 * 학생 활동 통계 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class StudentActivityStatService {

    private final StudentActivityStatRepository repository;

    public StudentActivityStatService(
            StudentActivityStatRepository repository) {

        this.repository = repository;
    }

    public Page<StudentActivityStatResponse> getStats(
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("studentCourseId").ascending()
            );

        return repository
            .findAll(pageable)
            .map(StudentActivityStatResponse::from);
    }

    public Page<StudentActivityStatResponse> getByJob(
            Long jobId,
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(page, size);

        return repository
            .findByJobId(jobId, pageable)
            .map(StudentActivityStatResponse::from);
    }

    public Page<StudentActivityStatResponse> getByStudentCourse(
            Long studentCourseId,
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(page, size);

        return repository
            .findByStudentCourseId(
                studentCourseId,
                pageable
            )
            .map(StudentActivityStatResponse::from);
    }
}