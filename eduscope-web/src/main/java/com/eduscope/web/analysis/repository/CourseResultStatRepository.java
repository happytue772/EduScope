package com.eduscope.web.analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.analysis.entity.CourseResultStat;
import com.eduscope.web.analysis.entity.CourseResultStatId;

/**
 * COURSE_RESULT_STAT 조회 Repository.
 */
public interface CourseResultStatRepository
        extends JpaRepository<
            CourseResultStat,
            CourseResultStatId> {

    Page<CourseResultStat> findByJobId(
        Long jobId,
        Pageable pageable
    );

    Page<CourseResultStat> findByCoursePresentationId(
        Long coursePresentationId,
        Pageable pageable
    );
}