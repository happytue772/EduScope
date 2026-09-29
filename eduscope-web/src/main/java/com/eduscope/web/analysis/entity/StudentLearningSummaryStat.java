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
 * 학생별 학습 종합 통계 Entity.
 */
@Entity
@Table(name = "STUDENT_LEARNING_SUMMARY_STAT")
@IdClass(StudentLearningSummaryStatId.class)
public class StudentLearningSummaryStat {

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Id
    @Column(name = "STUDENT_COURSE_ID", nullable = false)
    private Long studentCourseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "JOB_ID",
        insertable = false,
        updatable = false
    )
    private AnalysisJob analysisJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "STUDENT_COURSE_ID",
        insertable = false,
        updatable = false
    )
    private StudentCourse studentCourse;

    @Column(name = "TOTAL_CLICK_COUNT", nullable = false)
    private Long totalClickCount;

    @Column(name = "ACTIVE_DAY_COUNT", nullable = false)
    private Long activeDayCount;

    @Column(name = "USED_MATERIAL_COUNT", nullable = false)
    private Long usedMaterialCount;

    @Column(name = "SUBMITTED_ASSESSMENT_COUNT", nullable = false)
    private Long submittedAssessmentCount;

    @Column(
        name = "AVG_ASSESSMENT_SCORE",
        precision = 7,
        scale = 2
    )
    private BigDecimal avgAssessmentScore;

    @Column(name = "FAILED_ASSESSMENT_COUNT", nullable = false)
    private Long failedAssessmentCount;

    @Column(name = "FIRST_ACTIVITY_DAY")
    private Integer firstActivityDay;

    @Column(name = "LAST_ACTIVITY_DAY")
    private Integer lastActivityDay;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected StudentLearningSummaryStat() {
        // JPA 기본 생성자
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getStudentCourseId() {
        return studentCourseId;
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

    public Long getSubmittedAssessmentCount() {
        return submittedAssessmentCount;
    }

    public BigDecimal getAvgAssessmentScore() {
        return avgAssessmentScore;
    }

    public Long getFailedAssessmentCount() {
        return failedAssessmentCount;
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