package com.eduscope.loader.reference.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.reference.entity.Assessment;

/**
 * ASSESSMENT 조회/저장 Repository.
 */
public interface AssessmentRepository
        extends JpaRepository<Assessment, Long> {

    Optional<Assessment>
        findByDatasetAndSourceAssessmentId(
            Dataset dataset,
            Long sourceAssessmentId
        );
}