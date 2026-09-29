package com.eduscope.loader.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.loader.analysis.entity.CourseResultStat;
import com.eduscope.loader.analysis.entity.CourseResultStatId;

public interface CourseResultStatRepository
        extends JpaRepository<CourseResultStat, CourseResultStatId> {

    long countByJobId(Long jobId);

    @Query("""
        select c.coursePresentationId
        from CourseResultStat c
        where c.jobId = :jobId
        """)
    List<Long> findCourseIdsByJobId(
        @Param("jobId") Long jobId
    );
}