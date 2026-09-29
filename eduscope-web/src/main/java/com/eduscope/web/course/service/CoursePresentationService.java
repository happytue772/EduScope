package com.eduscope.web.course.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.course.dto.CoursePresentationResponse;
import com.eduscope.web.course.entity.CoursePresentation;
import com.eduscope.web.course.repository.CoursePresentationRepository;

/**
 * 강의 조회 비즈니스 로직.
 */
@Service
@Transactional(readOnly = true)
public class CoursePresentationService {

    private final CoursePresentationRepository courseRepository;

    public CoursePresentationService(
            CoursePresentationRepository courseRepository) {

        this.courseRepository = courseRepository;
    }

    /**
     * 전체 강의 조회.
     */
    public List<CoursePresentationResponse> getCourses() {

        return courseRepository
            .findAllByOrderByCodeModuleAscCodePresentationAsc()
            .stream()
            .map(CoursePresentationResponse::from)
            .toList();
    }

    /**
     * 강의 PK 기준 단건 조회.
     */
    public CoursePresentationResponse getCourse(
            Long coursePresentationId) {

        CoursePresentation course =
            courseRepository
                .findById(coursePresentationId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "COURSE_PRESENTATION을 찾을 수 없습니다. id="
                        + coursePresentationId
                    )
                );

        return CoursePresentationResponse.from(course);
    }
}