package com.eduscope.loader.analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.loader.analysis.entity.DataQualityStat;
import com.eduscope.loader.analysis.entity.DataQualityStatId;

/**
 * DATA_QUALITY_STAT Repository.
 */
public interface DataQualityStatRepository
        extends JpaRepository<
            DataQualityStat,
            DataQualityStatId> {

    boolean existsByJobIdAndDatasetFileIdAndQualityType(
        Long jobId,
        Long datasetFileId,
        String qualityType
    );

    @Query("""
        select coalesce(sum(d.recordCount), 0)
        from DataQualityStat d
        where d.jobId = :jobId
        """)
    Long sumRecordCountByJobId(
        @Param("jobId") Long jobId
    );

    long countByJobId(Long jobId);
}