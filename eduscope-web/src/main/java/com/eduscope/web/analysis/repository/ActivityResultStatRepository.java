package com.eduscope.web.analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.analysis.entity.ActivityResultStat;
import com.eduscope.web.analysis.entity.ActivityResultStatId;

/**
 * ACTIVITY_RESULT_STAT 조회 Repository.
 */
public interface ActivityResultStatRepository
        extends JpaRepository<
            ActivityResultStat,
            ActivityResultStatId> {

    Page<ActivityResultStat> findByJobId(
        Long jobId,
        Pageable pageable
    );

    Page<ActivityResultStat> findByCoursePresentationId(
        Long coursePresentationId,
        Pageable pageable
    );

    Page<ActivityResultStat> findByFinalResult(
        String finalResult,
        Pageable pageable
    );
}