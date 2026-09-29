package com.eduscope.web.resultanalysis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.resultanalysis.dto.ResultAnalysisResponse;
import com.eduscope.web.resultanalysis.repository.ResultAnalysisRepository;

/**
 * 최종성과 비교 Service.
 */
@Service
@Transactional(readOnly = true)
public class ResultAnalysisService {

    private final ResultAnalysisRepository repository;

    public ResultAnalysisService(
            ResultAnalysisRepository repository) {

        this.repository = repository;
    }

    public ResultAnalysisResponse getAnalysis(
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

        ResultAnalysisResponse.CourseInfo course =
            repository.findCourse(
                coursePresentationId
            );

        Long activityResultJobId =
            repository.findLatestActivityResultJobId(
                course.getDatasetId()
            );

        Long courseResultJobId =
            repository.findLatestCourseResultJobId(
                course.getDatasetId()
            );

        ResultAnalysisResponse.CourseResultSummary courseResult =
            null;

        if (courseResultJobId != null) {

            courseResult =
                repository.findCourseResult(
                    coursePresentationId,
                    courseResultJobId
                );
        }

        List<ResultAnalysisResponse.ResultActivityItem> resultActivities =
            activityResultJobId == null
                ? List.of()
                : repository.findResultActivities(
                    coursePresentationId,
                    activityResultJobId
                );

        return new ResultAnalysisResponse(
            course,
            activityResultJobId,
            courseResultJobId,
            courseResult,
            resultActivities
        );
    }
}