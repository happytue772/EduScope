package com.eduscope.web.analysis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.CourseWeeklyActivityStatResponse;
import com.eduscope.web.analysis.repository.CourseWeeklyActivityStatRepository;

/**
 * 강의별 주차 활동통계 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class CourseWeeklyActivityStatService {

    private final CourseWeeklyActivityStatRepository repository;

    public CourseWeeklyActivityStatService(
            CourseWeeklyActivityStatRepository repository) {

        this.repository = repository;
    }

    public Page<CourseWeeklyActivityStatResponse> getStats(
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("coursePresentationId")
                    .ascending()
                    .and(
                        Sort.by("relativeWeekNo").ascending()
                    )
            );

        return repository
            .findAll(pageable)
            .map(CourseWeeklyActivityStatResponse::from);
    }

    public Page<CourseWeeklyActivityStatResponse> getByJob(
            Long jobId,
            int page,
            int size) {

        return repository
            .findByJobId(
                jobId,
                PageRequest.of(page, size)
            )
            .map(CourseWeeklyActivityStatResponse::from);
    }

    public Page<CourseWeeklyActivityStatResponse> getByCourse(
            Long coursePresentationId,
            int page,
            int size) {

        return repository
            .findByCoursePresentationId(
                coursePresentationId,
                PageRequest.of(
                    page,
                    size,
                    Sort.by("relativeWeekNo").ascending()
                )
            )
            .map(CourseWeeklyActivityStatResponse::from);
    }

    public Page<CourseWeeklyActivityStatResponse> getByCourseAndWeek(
            Long coursePresentationId,
            Integer relativeWeekNo,
            int page,
            int size) {

        return repository
            .findByCoursePresentationIdAndRelativeWeekNo(
                coursePresentationId,
                relativeWeekNo,
                PageRequest.of(page, size)
            )
            .map(CourseWeeklyActivityStatResponse::from);
    }
}