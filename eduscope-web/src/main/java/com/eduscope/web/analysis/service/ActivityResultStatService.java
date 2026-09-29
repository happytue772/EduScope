package com.eduscope.web.analysis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.ActivityResultStatResponse;
import com.eduscope.web.analysis.repository.ActivityResultStatRepository;

/**
 * 학습결과별 활동 통계 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class ActivityResultStatService {

    private final ActivityResultStatRepository repository;

    public ActivityResultStatService(
            ActivityResultStatRepository repository) {

        this.repository = repository;
    }

    public Page<ActivityResultStatResponse> getStats(
            int page,
            int size) {

        return repository
            .findAll(PageRequest.of(page, size))
            .map(ActivityResultStatResponse::from);
    }

    public Page<ActivityResultStatResponse> getByJob(
            Long jobId,
            int page,
            int size) {

        return repository
            .findByJobId(
                jobId,
                PageRequest.of(page, size)
            )
            .map(ActivityResultStatResponse::from);
    }

    public Page<ActivityResultStatResponse> getByCourse(
            Long coursePresentationId,
            int page,
            int size) {

        return repository
            .findByCoursePresentationId(
                coursePresentationId,
                PageRequest.of(page, size)
            )
            .map(ActivityResultStatResponse::from);
    }

    public Page<ActivityResultStatResponse> getByResult(
            String finalResult,
            int page,
            int size) {

        return repository
            .findByFinalResult(
                finalResult,
                PageRequest.of(page, size)
            )
            .map(ActivityResultStatResponse::from);
    }
}