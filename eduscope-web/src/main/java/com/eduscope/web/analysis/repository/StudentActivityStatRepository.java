package com.eduscope.web.analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.analysis.entity.StudentActivityStat;
import com.eduscope.web.analysis.entity.StudentActivityStatId;

/**
 * STUDENT_ACTIVITY_STAT 조회 Repository.
 */
public interface StudentActivityStatRepository
        extends JpaRepository<
            StudentActivityStat,
            StudentActivityStatId> {

    /**
     * 특정 분석 Job의 결과.
     */
    Page<StudentActivityStat>
        findByJobId(
            Long jobId,
            Pageable pageable
        );

    /**
     * 특정 학생 수강 건의 활동 통계.
     */
    Page<StudentActivityStat>
        findByStudentCourseId(
            Long studentCourseId,
            Pageable pageable
        );
}