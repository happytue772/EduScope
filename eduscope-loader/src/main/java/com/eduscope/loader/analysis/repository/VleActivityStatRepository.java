package com.eduscope.loader.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.loader.analysis.entity.VleActivityStat;
import com.eduscope.loader.analysis.entity.VleActivityStatId;

/**
 * VLE_ACTIVITY_STAT Repository.
 */
public interface VleActivityStatRepository
        extends JpaRepository<
            VleActivityStat,
            VleActivityStatId> {

    long countByJobId(Long jobId);

    /**
     * 재실행 시 기존 VLE_MATERIAL_ID를 한 번에 조회한다.
     */
    @Query("""
        select v.vleMaterialId
        from VleActivityStat v
        where v.jobId = :jobId
        """)
    List<Long> findVleMaterialIdsByJobId(
        @Param("jobId") Long jobId
    );
}