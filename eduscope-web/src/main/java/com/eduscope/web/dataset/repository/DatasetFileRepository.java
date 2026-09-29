package com.eduscope.web.dataset.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.dataset.entity.DatasetFile;

/**
 * DATASET_FILE 조회/관리 Repository.
 */
public interface DatasetFileRepository
        extends JpaRepository<DatasetFile, Long> {

    List<DatasetFile>
    findByDataset_DatasetIdOrderByDatasetFileIdAsc(
        Long datasetId
    );

    boolean existsByDataset_DatasetIdAndFileType(
        Long datasetId,
        String fileType
    );

    long countByDataset_DatasetId(
        Long datasetId
    );
}
