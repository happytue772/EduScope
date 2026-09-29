package com.eduscope.web.analysis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.CourseActivityStatResponse;
import com.eduscope.web.analysis.repository.CourseActivityStatRepository;

/**
 * 강의 활동통계 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class CourseActivityStatService {

    private final CourseActivityStatRepository repository;

    public CourseActivityStatService(
            CourseActivityStatRepository repository) {

        this.repository = repository;
    }

    public Page<CourseActivityStatResponse> getStats(
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("coursePresentationId").ascending()
            );

        return repository
            .findAll(pageable)
            .map(CourseActivityStatResponse::from);
    }

    public Page<CourseActivityStatResponse> getByJob(
            Long jobId,
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("coursePresentationId").ascending()
            );

        return repository
            .findByJobId(jobId, pageable)
            .map(CourseActivityStatResponse::from);
    }

    public Page<CourseActivityStatResponse> getByCourse(
            Long coursePresentationId,
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(page, size);

        return repository
            .findByCoursePresentationId(
                coursePresentationId,
                pageable
            )
            .map(CourseActivityStatResponse::from);
    }
}