package com.eduscope.web.analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.analysis.entity.RegistrationStat;
import com.eduscope.web.analysis.entity.RegistrationStatId;

/**
 * REGISTRATION_STAT 조회 Repository.
 */
public interface RegistrationStatRepository
        extends JpaRepository<
            RegistrationStat,
            RegistrationStatId> {

    Page<RegistrationStat> findByJobId(
        Long jobId,
        Pageable pageable
    );

    Page<RegistrationStat> findByCoursePresentationId(
        Long coursePresentationId,
        Pageable pageable
    );
}