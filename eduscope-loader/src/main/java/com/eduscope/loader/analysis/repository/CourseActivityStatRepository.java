package com.eduscope.loader.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.loader.analysis.entity.CourseActivityStat;
import com.eduscope.loader.analysis.entity.CourseActivityStatId;

/**
 * COURSE_ACTIVITY_STAT Repository.
 */
public interface CourseActivityStatRepository
        extends JpaRepository<
            CourseActivityStat,
            CourseActivityStatId> {

    long countByJobId(Long jobId);

    @Query("""
        select c.coursePresentationId
        from CourseActivityStat c
        where c.jobId = :jobId
        """)
    List<Long> findCoursePresentationIdsByJobId(
        @Param("jobId") Long jobId
    );
}