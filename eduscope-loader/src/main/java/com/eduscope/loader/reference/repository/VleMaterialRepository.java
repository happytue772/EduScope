package com.eduscope.loader.reference.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.entity.VleMaterial;

/**
 * VLE_MATERIAL Repository.
 */
public interface VleMaterialRepository
        extends JpaRepository<VleMaterial, Long> {

    /**
     * 실제 UK_VLE_MATERIAL 기준 중복 확인.
     */
    Optional<VleMaterial>
        findByDatasetAndCoursePresentationAndSourceSiteId(
            Dataset dataset,
            CoursePresentation coursePresentation,
            Long sourceSiteId
        );
}