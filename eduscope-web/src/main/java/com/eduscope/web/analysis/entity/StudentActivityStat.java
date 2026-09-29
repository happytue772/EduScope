package com.eduscope.web.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eduscope.web.student.entity.StudentCourse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 학생별 VLE 활동 집계 통계.
 */
@Entity
@Table(name = "STUDENT_ACTIVITY_STAT")
@IdClass(StudentActivityStatId.class)
public class StudentActivityStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(
        name = "STUDENT_COURSE_ID",
        nullable = false
    )
    private Long studentCourseId;

    /**
     * JOB_ID를 이용한 읽기 전용 관계.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "JOB_ID",
        insertable = false,
        updatable = false
    )
    private AnalysisJob analysisJob;

    /**
     * STUDENT_COURSE_ID 읽기 전용 관계.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "STUDENT_COURSE_ID",
        insertable = false,
        updatable = false
    )
    private StudentCourse studentCourse;

    @Column(
        name = "TOTAL_CLICK_COUNT",
        nullable = false
    )
    private Long totalClickCount;

    @Column(
        name = "ACTIVE_DAY_COUNT",
        nullable = false
    )
    private Long activeDayCount;

    @Column(
        name = "USED_MATERIAL_COUNT",
        nullable = false
    )
    private Long usedMaterialCount;

    @Column(
        name = "AVG_DAILY_CLICK_COUNT",
        nullable = false,
        precision = 19,
        scale = 4
    )
    private BigDecimal avgDailyClickCount;

    @Column(name = "FIRST_ACTIVITY_DAY")
    private Integer firstActivityDay;

    @Column(name = "LAST_ACTIVITY_DAY")
    private Integer lastActivityDay;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected StudentActivityStat() {
        // JPA 기본 생성자
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getStudentCourseId() {
        return studentCourseId;
    }

    public AnalysisJob getAnalysisJob() {
        return analysisJob;
    }

    public StudentCourse getStudentCourse() {
        return studentCourse;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public Long getActiveDayCount() {
        return activeDayCount;
    }

    public Long getUsedMaterialCount() {
        return usedMaterialCount;
    }

    public BigDecimal getAvgDailyClickCount() {
        return avgDailyClickCount;
    }

    public Integer getFirstActivityDay() {
        return firstActivityDay;
    }

    public Integer getLastActivityDay() {
        return lastActivityDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}