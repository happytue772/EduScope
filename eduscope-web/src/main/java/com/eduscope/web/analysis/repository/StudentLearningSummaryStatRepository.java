package com.eduscope.web.analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.analysis.entity.StudentLearningSummaryStat;
import com.eduscope.web.analysis.entity.StudentLearningSummaryStatId;

/**
 * STUDENT_LEARNING_SUMMARY_STAT 조회 Repository.
 */
public interface StudentLearningSummaryStatRepository
        extends JpaRepository<
            StudentLearningSummaryStat,
            StudentLearningSummaryStatId> {

    Page<StudentLearningSummaryStat> findByJobId(
        Long jobId,
        Pageable pageable
    );

    Page<StudentLearningSummaryStat> findByStudentCourseId(
        Long studentCourseId,
        Pageable pageable
    );
}