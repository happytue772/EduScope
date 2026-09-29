package com.eduscope.web.analysis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.AssessmentStatResponse;
import com.eduscope.web.analysis.repository.AssessmentStatRepository;

/**
 * 평가 통계 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class AssessmentStatService {

    private final AssessmentStatRepository repository;

    public AssessmentStatService(
            AssessmentStatRepository repository) {

        this.repository = repository;
    }

    /**
     * 전체 평가 통계.
     */
    public Page<AssessmentStatResponse> getStats(
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("assessmentId").ascending()
            );

        return repository
            .findAll(pageable)
            .map(AssessmentStatResponse::from);
    }

    /**
     * 특정 분석 Job 결과.
     */
    public Page<AssessmentStatResponse> getByJob(
            Long jobId,
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("assessmentId").ascending()
            );

        return repository
            .findByJobId(
                jobId,
                pageable
            )
            .map(AssessmentStatResponse::from);
    }

    /**
     * 특정 평가 통계.
     */
    public Page<AssessmentStatResponse> getByAssessment(
            Long assessmentId,
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(page, size);

        return repository
            .findByAssessmentId(
                assessmentId,
                pageable
            )
            .map(AssessmentStatResponse::from);
    }
}