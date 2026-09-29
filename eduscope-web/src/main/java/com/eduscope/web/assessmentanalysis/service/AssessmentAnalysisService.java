package com.eduscope.web.assessmentanalysis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.assessmentanalysis.dto.AssessmentAnalysisResponse;
import com.eduscope.web.assessmentanalysis.repository.AssessmentAnalysisRepository;

/**
 * 평가 분석 Service.
 */
@Service
@Transactional(readOnly = true)
public class AssessmentAnalysisService {

    private final AssessmentAnalysisRepository repository;

    public AssessmentAnalysisService(
            AssessmentAnalysisRepository repository) {

        this.repository = repository;
    }


    public AssessmentAnalysisResponse getAnalysis(
            Long coursePresentationId) {

        if (
            coursePresentationId == null
            ||
            coursePresentationId <= 0
        ) {

            throw new IllegalArgumentException(
                "coursePresentationId는 "
                + "1 이상의 값이어야 합니다."
            );
        }

        AssessmentAnalysisResponse.CourseInfo course =
            repository.findCourse(
                coursePresentationId
            );

        Long jobId =
            repository.findLatestAssessmentJobId(
                course.getDatasetId()
            );

        if (jobId == null) {

            return new AssessmentAnalysisResponse(
                course,
                null,
                List.of()
            );
        }

        List<AssessmentAnalysisResponse.AssessmentItem>
            assessments =
                repository.findAssessments(
                    coursePresentationId,
                    jobId
                );

        return new AssessmentAnalysisResponse(
            course,
            jobId,
            assessments
        );
    }
}