package com.eduscope.web.analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.analysis.entity.DataQualityStat;
import com.eduscope.web.analysis.entity.DataQualityStatId;

/**
 * DATA_QUALITY_STAT 조회 Repository.
 */
public interface DataQualityStatRepository
        extends JpaRepository<
            DataQualityStat,
            DataQualityStatId> {

    Page<DataQualityStat> findByJobId(
        Long jobId,
        Pageable pageable
    );

    Page<DataQualityStat> findByDatasetFileId(
        Long datasetFileId,
        Pageable pageable
    );

    Page<DataQualityStat> findByQualityType(
        String qualityType,
        Pageable pageable
    );
}