package com.eduscope.web.analysis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.CourseResultStatResponse;
import com.eduscope.web.analysis.repository.CourseResultStatRepository;

/**
 * 강의 결과 통계 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class CourseResultStatService {

    private final CourseResultStatRepository repository;

    public CourseResultStatService(
            CourseResultStatRepository repository) {

        this.repository = repository;
    }

    public Page<CourseResultStatResponse> getStats(
            int page,
            int size) {

        return repository
            .findAll(PageRequest.of(page, size))
            .map(CourseResultStatResponse::from);
    }

    public Page<CourseResultStatResponse> getByJob(
            Long jobId,
            int page,
            int size) {

        return repository
            .findByJobId(
                jobId,
                PageRequest.of(page, size)
            )
            .map(CourseResultStatResponse::from);
    }

    public Page<CourseResultStatResponse> getByCourse(
            Long coursePresentationId,
            int page,
            int size) {

        return repository
            .findByCoursePresentationId(
                coursePresentationId,
                PageRequest.of(page, size)
            )
            .map(CourseResultStatResponse::from);
    }
}