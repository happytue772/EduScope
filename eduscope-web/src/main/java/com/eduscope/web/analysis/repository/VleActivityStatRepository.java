package com.eduscope.web.analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.analysis.entity.VleActivityStat;
import com.eduscope.web.analysis.entity.VleActivityStatId;

/**
 * VLE_ACTIVITY_STAT 조회 Repository.
 */
public interface VleActivityStatRepository
        extends JpaRepository<
            VleActivityStat,
            VleActivityStatId> {

    /**
     * 특정 분석 Job 결과 조회.
     */
    Page<VleActivityStat> findByJobId(
        Long jobId,
        Pageable pageable
    );

    /**
     * 특정 VLE 자료의 활동통계 조회.
     */
    Page<VleActivityStat> findByVleMaterialId(
        Long vleMaterialId,
        Pageable pageable
    );
}