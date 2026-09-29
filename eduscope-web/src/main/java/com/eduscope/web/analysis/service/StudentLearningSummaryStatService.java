package com.eduscope.web.analysis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.StudentLearningSummaryStatResponse;
import com.eduscope.web.analysis.repository.StudentLearningSummaryStatRepository;

/**
 * 학생 학습 종합 통계 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class StudentLearningSummaryStatService {

    private final StudentLearningSummaryStatRepository repository;

    public StudentLearningSummaryStatService(
            StudentLearningSummaryStatRepository repository) {

        this.repository = repository;
    }

    public Page<StudentLearningSummaryStatResponse> getStats(
            int page,
            int size) {

        return repository
            .findAll(
                PageRequest.of(
                    page,
                    size,
                    Sort.by("studentCourseId").ascending()
                )
            )
            .map(StudentLearningSummaryStatResponse::from);
    }

    public Page<StudentLearningSummaryStatResponse> getByJob(
            Long jobId,
            int page,
            int size) {

        return repository
            .findByJobId(
                jobId,
                PageRequest.of(page, size)
            )
            .map(StudentLearningSummaryStatResponse::from);
    }

    public Page<StudentLearningSummaryStatResponse> getByStudentCourse(
            Long studentCourseId,
            int page,
            int size) {

        return repository
            .findByStudentCourseId(
                studentCourseId,
                PageRequest.of(page, size)
            )
            .map(StudentLearningSummaryStatResponse::from);
    }
}