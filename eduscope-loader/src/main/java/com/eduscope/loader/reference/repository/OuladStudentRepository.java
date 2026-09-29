package com.eduscope.loader.reference.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.reference.entity.OuladStudent;

public interface OuladStudentRepository
        extends JpaRepository<OuladStudent, Long> {

    Optional<OuladStudent>
        findByDatasetAndSourceStudentId(
            Dataset dataset,
            Long sourceStudentId
        );
}