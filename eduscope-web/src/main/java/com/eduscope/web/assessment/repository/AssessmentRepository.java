package com.eduscope.web.assessment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.assessment.entity.Assessment;

/**
 * ASSESSMENT 조회 Repository.
 */
public interface AssessmentRepository
        extends JpaRepository<Assessment, Long> {

    /** 목록 DTO에서 사용하는 강의 코드를 함께 조회한다. */
    @Override
    @EntityGraph(attributePaths = "coursePresentation")
    List<Assessment> findAll();

    /**
     * 특정 강의의 평가 목록.
     */
    @EntityGraph(attributePaths = "coursePresentation")
    List<Assessment>
        findByCoursePresentation_CoursePresentationIdOrderByAssessmentIdAsc(
            Long coursePresentationId
        );

    /**
     * OULAD 원본 평가 ID 기준 조회.
     */
    Optional<Assessment>
        findByDataset_DatasetIdAndSourceAssessmentId(
            Long datasetId,
            Long sourceAssessmentId
        );
}
