package com.eduscope.web.vle.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.vle.dto.VleMaterialResponse;
import com.eduscope.web.vle.entity.VleMaterial;
import com.eduscope.web.vle.repository.VleMaterialRepository;

/**
 * VLE 학습자료 조회 비즈니스 로직.
 */
@Service
@Transactional(readOnly = true)
public class VleMaterialService {

    private final VleMaterialRepository repository;

    public VleMaterialService(
            VleMaterialRepository repository) {

        this.repository = repository;
    }

    /**
     * 전체 VLE 자료 페이지 조회.
     */
    public Page<VleMaterialResponse> getMaterials(
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
            .map(VleMaterialResponse::from);
    }

    /**
     * VLE_MATERIAL_ID 단건 조회.
     */
    public VleMaterialResponse getMaterial(
            Long vleMaterialId) {

        VleMaterial material =
            repository
                .findById(vleMaterialId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "VLE_MATERIAL을 찾을 수 없습니다. id="
                        + vleMaterialId
                    )
                );

        return VleMaterialResponse.from(material);
    }

    /**
     * 특정 강의에 속한 VLE 자료 조회.
     */
    public Page<VleMaterialResponse> getMaterialsByCourse(
            Long coursePresentationId,
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("vleMaterialId").ascending()
            );

        return repository
            .findByCoursePresentation_CoursePresentationId(
                coursePresentationId,
                pageable
            )
            .map(VleMaterialResponse::from);
    }
}