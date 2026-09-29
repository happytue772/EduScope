package com.eduscope.web.analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.analysis.entity.AssessmentStat;
import com.eduscope.web.analysis.entity.AssessmentStatId;

/**
 * ASSESSMENT_STAT 조회 Repository.
 */
public interface AssessmentStatRepository
        extends JpaRepository<
            AssessmentStat,
            AssessmentStatId> {

    /**
     * 특정 분석 Job 결과.
     */
    Page<AssessmentStat> findByJobId(
        Long jobId,
        Pageable pageable
    );

    /**
     * 특정 평가의 분석 결과.
     */
    Page<AssessmentStat> findByAssessmentId(
        Long assessmentId,
        Pageable pageable
    );
}