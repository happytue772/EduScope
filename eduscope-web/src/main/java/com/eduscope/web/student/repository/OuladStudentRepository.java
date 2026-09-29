package com.eduscope.web.student.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.student.entity.OuladStudent;

/**
 * OULAD_STUDENT 조회 Repository.
 */
public interface OuladStudentRepository
        extends JpaRepository<OuladStudent, Long> {

    /**
     * OULAD 원본 학생 ID로 조회.
     */
    Optional<OuladStudent> findByDataset_DatasetIdAndSourceStudentId(
        Long datasetId,
        Long sourceStudentId
    );
}