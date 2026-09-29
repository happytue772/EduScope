package com.eduscope.loader.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.loader.analysis.entity.CourseWeeklyActivityStat;
import com.eduscope.loader.analysis.entity.CourseWeeklyActivityStatId;

/**
 * COURSE_WEEKLY_ACTIVITY_STAT Repository.
 */
public interface CourseWeeklyActivityStatRepository
        extends JpaRepository<
            CourseWeeklyActivityStat,
            CourseWeeklyActivityStatId> {

    long countByJobId(Long jobId);

    /**
     * 동일 Job의 기존 적재 결과를 한 번에 읽는다.
     * 재실행 시 복합 PK 중복 방지용.
     */
    List<CourseWeeklyActivityStat>
        findAllByJobId(Long jobId);
}