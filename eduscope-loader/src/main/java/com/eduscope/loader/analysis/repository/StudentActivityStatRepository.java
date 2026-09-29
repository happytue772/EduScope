package com.eduscope.loader.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.loader.analysis.entity.StudentActivityStat;
import com.eduscope.loader.analysis.entity.StudentActivityStatId;

/**
 * STUDENT_ACTIVITY_STAT Repository.
 */
public interface StudentActivityStatRepository
        extends JpaRepository<
            StudentActivityStat,
            StudentActivityStatId> {

    long countByJobId(Long jobId);

    /**
     * 재실행 시 기존 적재 키를 한 번에 읽어서
     * 행마다 DB 중복 조회하는 것을 방지한다.
     */
    @Query("""
        select s.studentCourseId
        from StudentActivityStat s
        where s.jobId = :jobId
        """)
    List<Long> findStudentCourseIdsByJobId(
        @Param("jobId") Long jobId
    );
}