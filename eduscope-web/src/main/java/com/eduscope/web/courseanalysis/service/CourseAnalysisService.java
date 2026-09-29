package com.eduscope.web.courseanalysis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.courseanalysis.dto.CourseAnalysisResponse;
import com.eduscope.web.courseanalysis.repository.CourseAnalysisRepository;

/**
 * 강의 분석 Service.
 */
@Service
@Transactional(readOnly = true)
public class CourseAnalysisService {

    private final CourseAnalysisRepository repository;

    public CourseAnalysisService(
            CourseAnalysisRepository repository) {

        this.repository = repository;
    }

    public CourseAnalysisResponse getCourseAnalysis(
            Long coursePresentationId) {

        if (coursePresentationId == null
                || coursePresentationId <= 0) {

            throw new IllegalArgumentException(
                "coursePresentationId는 1 이상의 값이어야 합니다."
            );
        }

        CourseAnalysisResponse.CourseInfo course =
            repository.findCourse(
                coursePresentationId
            );

        Long datasetId =
            course.getDatasetId();

        CourseAnalysisResponse.ActivitySummary activity =
            repository.findActivity(
                coursePresentationId,
                datasetId
            );

        CourseAnalysisResponse.ResultSummary result =
            repository.findResult(
                coursePresentationId,
                datasetId
            );

        CourseAnalysisResponse.RegistrationSummary registration =
            repository.findRegistration(
                coursePresentationId,
                datasetId
            );

        List<CourseAnalysisResponse.WeeklyActivity> weeklyActivity =
            repository.findWeeklyActivity(
                coursePresentationId,
                datasetId
            );

        return new CourseAnalysisResponse(
            course,
            activity,
            result,
            registration,
            weeklyActivity
        );
    }
}