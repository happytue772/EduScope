package com.eduscope.loader.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.loader.analysis.entity.AssessmentStat;
import com.eduscope.loader.analysis.entity.AssessmentStatId;

/**
 * ASSESSMENT_STAT Repository.
 */
public interface AssessmentStatRepository
        extends JpaRepository<
            AssessmentStat,
            AssessmentStatId> {

    long countByJobId(Long jobId);

    /**
     * 같은 Job의 기존 ASSESSMENT_ID 조회.
     */
    @Query("""
        select a.assessmentId
        from AssessmentStat a
        where a.jobId = :jobId
        """)
    List<Long> findAssessmentIdsByJobId(
        @Param("jobId") Long jobId
    );
}