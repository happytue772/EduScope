package com.eduscope.web.vle.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.vle.entity.VleMaterial;

/**
 * VLE_MATERIAL 조회 Repository.
 */
public interface VleMaterialRepository
        extends JpaRepository<VleMaterial, Long> {

    /** 목록 DTO에서 사용하는 강의 코드를 함께 조회한다. */
    @Override
    @EntityGraph(attributePaths = "coursePresentation")
    Page<VleMaterial> findAll(Pageable pageable);

    /**
     * 특정 강의의 VLE 자료 조회.
     */
    @EntityGraph(attributePaths = "coursePresentation")
    Page<VleMaterial>
        findByCoursePresentation_CoursePresentationId(
            Long coursePresentationId,
            Pageable pageable
        );
}
