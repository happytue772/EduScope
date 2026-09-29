package com.eduscope.web.analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.analysis.entity.CourseWeeklyActivityStat;
import com.eduscope.web.analysis.entity.CourseWeeklyActivityStatId;

/**
 * COURSE_WEEKLY_ACTIVITY_STAT 조회 Repository.
 */
public interface CourseWeeklyActivityStatRepository
        extends JpaRepository<
            CourseWeeklyActivityStat,
            CourseWeeklyActivityStatId> {

    Page<CourseWeeklyActivityStat> findByJobId(
        Long jobId,
        Pageable pageable
    );

    Page<CourseWeeklyActivityStat>
        findByCoursePresentationId(
            Long coursePresentationId,
            Pageable pageable
        );

    Page<CourseWeeklyActivityStat>
        findByCoursePresentationIdAndRelativeWeekNo(
            Long coursePresentationId,
            Integer relativeWeekNo,
            Pageable pageable
        );
}