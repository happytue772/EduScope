package com.eduscope.loader.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.loader.analysis.entity.StudentLearningSummaryStat;
import com.eduscope.loader.analysis.entity.StudentLearningSummaryStatId;

/**
 * STUDENT_LEARNING_SUMMARY_STAT Repository.
 */
public interface StudentLearningSummaryStatRepository
        extends JpaRepository<
            StudentLearningSummaryStat,
            StudentLearningSummaryStatId> {

    long countByJobId(Long jobId);

    /**
     * 재실행 시 기존 학생-수강 ID를 한 번에 조회.
     */
    @Query("""
        select s.studentCourseId
        from StudentLearningSummaryStat s
        where s.jobId = :jobId
        """)
    List<Long> findStudentCourseIdsByJobId(
        @Param("jobId") Long jobId
    );
}