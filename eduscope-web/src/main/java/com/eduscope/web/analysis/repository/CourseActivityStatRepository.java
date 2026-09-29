package com.eduscope.web.analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.analysis.entity.CourseActivityStat;
import com.eduscope.web.analysis.entity.CourseActivityStatId;

/**
 * COURSE_ACTIVITY_STAT 조회 Repository.
 */
public interface CourseActivityStatRepository
        extends JpaRepository<
            CourseActivityStat,
            CourseActivityStatId> {

    Page<CourseActivityStat> findByJobId(
        Long jobId,
        Pageable pageable
    );

    Page<CourseActivityStat> findByCoursePresentationId(
        Long coursePresentationId,
        Pageable pageable
    );
}
