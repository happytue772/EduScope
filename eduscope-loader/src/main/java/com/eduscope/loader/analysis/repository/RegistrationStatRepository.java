package com.eduscope.loader.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.loader.analysis.entity.RegistrationStat;
import com.eduscope.loader.analysis.entity.RegistrationStatId;

/**
 * REGISTRATION_STAT Repository.
 */
public interface RegistrationStatRepository
        extends JpaRepository<
            RegistrationStat,
            RegistrationStatId> {

    long countByJobId(Long jobId);

    @Query("""
        select r.coursePresentationId
        from RegistrationStat r
        where r.jobId = :jobId
        """)
    List<Long> findCoursePresentationIdsByJobId(
        @Param("jobId") Long jobId
    );
}