package com.eduscope.web.dataset.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.dataset.entity.Dataset;

/**
 * DATASET 테이블 조회/관리 Repository.
 */
public interface DatasetRepository
        extends JpaRepository<Dataset, Long> {

    List<Dataset> findByIsDeletedOrderByDatasetIdAsc(
        String isDeleted
    );

    Optional<Dataset> findBySourceNameAndDatasetVersion(
        String sourceName,
        String datasetVersion
    );
}
