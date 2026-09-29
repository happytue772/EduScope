package com.eduscope.web.analysis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.RegistrationStatResponse;
import com.eduscope.web.analysis.repository.RegistrationStatRepository;

/**
 * 등록/수강취소 통계 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class RegistrationStatService {

    private final RegistrationStatRepository repository;

    public RegistrationStatService(
            RegistrationStatRepository repository) {

        this.repository = repository;
    }

    public Page<RegistrationStatResponse> getStats(
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
            .map(RegistrationStatResponse::from);
    }

    public Page<RegistrationStatResponse> getByJob(
            Long jobId,
            int page,
            int size) {

        return repository
            .findByJobId(
                jobId,
                PageRequest.of(page, size)
            )
            .map(RegistrationStatResponse::from);
    }

    public Page<RegistrationStatResponse> getByCourse(
            Long coursePresentationId,
            int page,
            int size) {

        return repository
            .findByCoursePresentationId(
                coursePresentationId,
                PageRequest.of(page, size)
            )
            .map(RegistrationStatResponse::from);
    }
}