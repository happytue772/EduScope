package com.eduscope.web.assessment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.assessment.dto.AssessmentResponse;
import com.eduscope.web.assessment.entity.Assessment;
import com.eduscope.web.assessment.repository.AssessmentRepository;

/**
 * 평가/과제 기준정보 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class AssessmentService {

    private final AssessmentRepository repository;

    public AssessmentService(
            AssessmentRepository repository) {

        this.repository = repository;
    }

    /**
     * 전체 평가 조회.
     */
    public List<AssessmentResponse> getAssessments() {

        return repository
            .findAll()
            .stream()
            .map(AssessmentResponse::from)
            .toList();
    }

    /**
     * ASSESSMENT_ID 기준 단건 조회.
     */
    public AssessmentResponse getAssessment(
            Long assessmentId) {

        Assessment assessment =
            repository
                .findById(assessmentId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "ASSESSMENT를 찾을 수 없습니다. assessmentId="
                        + assessmentId
                    )
                );

        return AssessmentResponse.from(assessment);
    }

    /**
     * 특정 강의의 평가 목록 조회.
     */
    public List<AssessmentResponse> getAssessmentsByCourse(
            Long coursePresentationId) {

        return repository
            .findByCoursePresentation_CoursePresentationIdOrderByAssessmentIdAsc(
                coursePresentationId
            )
            .stream()
            .map(AssessmentResponse::from)
            .toList();
    }
}