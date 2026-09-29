package com.eduscope.web.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.course.entity.CoursePresentation;

/**
 * COURSE_PRESENTATION 조회 Repository.
 */
public interface CoursePresentationRepository
        extends JpaRepository<CoursePresentation, Long> {

    /**
     * 강의코드 → 개설학기 순으로 정렬 조회.
     */
    List<CoursePresentation>
        findAllByOrderByCodeModuleAscCodePresentationAsc();
}