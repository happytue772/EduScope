package com.eduscope.web.studentanalysis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.studentanalysis.dto.StudentAnalysisResponse;
import com.eduscope.web.studentanalysis.dto.StudentSearchResponse;
import com.eduscope.web.studentanalysis.repository.StudentAnalysisRepository;

/**
 * 학생 분석 Service.
 */
@Service
@Transactional(readOnly = true)
public class StudentAnalysisService {

    private final StudentAnalysisRepository repository;

    public StudentAnalysisService(
            StudentAnalysisRepository repository) {

        this.repository = repository;
    }


    public List<StudentSearchResponse> search(
            String keyword,
            Long coursePresentationId,
            Integer offset,
            Integer limit) {

        int safeOffset =
            offset == null
                ? 0
                : Math.max(
                    0,
                    offset
                );

        int safeLimit =
            limit == null
                ? 100
                : Math.min(
                    Math.max(
                        1,
                        limit
                    ),
                    500
                );

        return repository.search(
            keyword,
            coursePresentationId,
            safeOffset,
            safeLimit
        );
    }


    public StudentAnalysisResponse getAnalysis(
            Long studentCourseId) {

        if (
            studentCourseId == null
            ||
            studentCourseId <= 0
        ) {

            throw new IllegalArgumentException(
                "studentCourseId는 "
                + "1 이상의 값이어야 합니다."
            );
        }

        StudentAnalysisResponse.StudentCourseInfo student =
            repository.findStudentCourse(
                studentCourseId
            );

        Long datasetId =
            student.getDatasetId();


        Long activityJobId =
            repository.findLatestJobId(
                datasetId,
                "STUDENT_ACTIVITY"
            );

        Long learningSummaryJobId =
            repository.findLatestJobId(
                datasetId,
                "STUDENT_LEARNING_SUMMARY"
            );


        StudentAnalysisResponse.ActivitySummary activity =
            activityJobId == null
                ? null
                : repository.findActivity(
                    studentCourseId,
                    activityJobId
                );


        StudentAnalysisResponse.LearningSummary learningSummary =
            learningSummaryJobId == null
                ? null
                : repository.findLearningSummary(
                    studentCourseId,
                    learningSummaryJobId
                );


        StudentAnalysisResponse.RegistrationInfo registration =
            repository.findRegistration(
                studentCourseId
            );


        List<StudentAnalysisResponse.AssessmentHistory> assessments =
            repository.findAssessments(
                studentCourseId
            );


        return new StudentAnalysisResponse(
            student,
            registration,
            activityJobId,
            learningSummaryJobId,
            activity,
            learningSummary,
            assessments
        );
    }
}