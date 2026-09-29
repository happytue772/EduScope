package com.eduscope.web.registrationanalysis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.registrationanalysis.dto.RegistrationAnalysisResponse;
import com.eduscope.web.registrationanalysis.repository.RegistrationAnalysisRepository;

/**
 * 수강/철회 분석 Service.
 */
@Service
@Transactional(readOnly = true)
public class RegistrationAnalysisService {

    private final RegistrationAnalysisRepository repository;

    public RegistrationAnalysisService(
            RegistrationAnalysisRepository repository) {

        this.repository = repository;
    }

    public RegistrationAnalysisResponse getAnalysis(
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

        RegistrationAnalysisResponse.CourseInfo course =
            repository.findCourse(
                coursePresentationId
            );

        Long jobId =
            repository.findLatestRegistrationJobId(
                course.getDatasetId()
            );

        RegistrationAnalysisResponse.RegistrationSummary summary =
            null;

        if (jobId != null) {

            summary =
                repository.findSummary(
                    coursePresentationId,
                    jobId
                );
        }

        /*
         * 원본 STUDENT_REGISTRATION 기반 분포는
         * Analysis Job 유무와 독립적으로 조회한다.
         */
        List<RegistrationAnalysisResponse.DailyCount> registrationByDay =
            repository.findRegistrationByDay(
                coursePresentationId
            );

        List<RegistrationAnalysisResponse.DailyCount> unregistrationByDay =
            repository.findUnregistrationByDay(
                coursePresentationId
            );

        return new RegistrationAnalysisResponse(
            course,
            jobId,
            summary,
            registrationByDay,
            unregistrationByDay
        );
    }
}