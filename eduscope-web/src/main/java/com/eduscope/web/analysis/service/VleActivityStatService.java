package com.eduscope.web.analysis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.VleActivityStatResponse;
import com.eduscope.web.analysis.repository.VleActivityStatRepository;

/**
 * VLE 학습자료 활동통계 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class VleActivityStatService {

    private final VleActivityStatRepository repository;

    public VleActivityStatService(
            VleActivityStatRepository repository) {

        this.repository = repository;
    }

    /**
     * 전체 통계 조회.
     */
    public Page<VleActivityStatResponse> getStats(
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("vleMaterialId").ascending()
            );

        return repository
            .findAll(pageable)
            .map(VleActivityStatResponse::from);
    }

    /**
     * 분석 Job별 조회.
     */
    public Page<VleActivityStatResponse> getByJob(
            Long jobId,
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("vleMaterialId").ascending()
            );

        return repository
            .findByJobId(jobId, pageable)
            .map(VleActivityStatResponse::from);
    }

    /**
     * VLE 자료별 조회.
     */
    public Page<VleActivityStatResponse> getByMaterial(
            Long vleMaterialId,
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(page, size);

        return repository
            .findByVleMaterialId(
                vleMaterialId,
                pageable
            )
            .map(VleActivityStatResponse::from);
    }
}