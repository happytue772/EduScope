package com.eduscope.web.activityanalysis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.activityanalysis.dto.ActivityAnalysisResponse;
import com.eduscope.web.activityanalysis.repository.ActivityAnalysisRepository;

/**
 * VLE 학습활동 분석 Service.
 */
@Service
@Transactional(readOnly = true)
public class ActivityAnalysisService {

    private final ActivityAnalysisRepository repository;

    public ActivityAnalysisService(
            ActivityAnalysisRepository repository) {

        this.repository = repository;
    }

    public ActivityAnalysisResponse getAnalysis(
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

        ActivityAnalysisResponse.CourseInfo course =
            repository.findCourse(
                coursePresentationId
            );

        Long jobId =
            repository.findLatestVleActivityJobId(
                course.getDatasetId()
            );

        if (jobId == null) {

            return new ActivityAnalysisResponse(
                course,
                null,
                List.of()
            );
        }

        List<ActivityAnalysisResponse.MaterialActivity> materials =
            repository.findMaterials(
                coursePresentationId,
                jobId
            );

        return new ActivityAnalysisResponse(
            course,
            jobId,
            materials
        );
    }
}