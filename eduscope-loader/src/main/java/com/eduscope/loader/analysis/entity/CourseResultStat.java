package com.eduscope.loader.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 강의별 최종 학습결과 통계.
 */
@Entity
@Table(name = "COURSE_RESULT_STAT")
@IdClass(CourseResultStatId.class)
public class CourseResultStat {

    @Id
    @Column(name = "JOB_ID")
    private Long jobId;

    @Id
    @Column(name = "COURSE_PRESENTATION_ID")
    private Long coursePresentationId;

    @Column(name = "STUDENT_COUNT", nullable = false)
    private Long studentCount;

    @Column(name = "PASS_COUNT", nullable = false)
    private Long passCount;

    @Column(name = "FAIL_COUNT", nullable = false)
    private Long failCount;

    @Column(name = "WITHDRAWN_COUNT", nullable = false)
    private Long withdrawnCount;

    @Column(name = "DISTINCTION_COUNT", nullable = false)
    private Long distinctionCount;

    @Column(name = "PASS_RATE", nullable = false,
            precision = 7, scale = 4)
    private BigDecimal passRate;

    @Column(name = "FAIL_RATE", nullable = false,
            precision = 7, scale = 4)
    private BigDecimal failRate;

    @Column(name = "WITHDRAWN_RATE", nullable = false,
            precision = 7, scale = 4)
    private BigDecimal withdrawnRate;

    @Column(name = "DISTINCTION_RATE", nullable = false,
            precision = 7, scale = 4)
    private BigDecimal distinctionRate;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public CourseResultStat() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public void setCoursePresentationId(Long value) {
        this.coursePresentationId = value;
    }

    public Long getStudentCount() { return studentCount; }
    public void setStudentCount(Long v) { studentCount = v; }

    public Long getPassCount() { return passCount; }
    public void setPassCount(Long v) { passCount = v; }

    public Long getFailCount() { return failCount; }
    public void setFailCount(Long v) { failCount = v; }

    public Long getWithdrawnCount() { return withdrawnCount; }
    public void setWithdrawnCount(Long v) { withdrawnCount = v; }

    public Long getDistinctionCount() { return distinctionCount; }
    public void setDistinctionCount(Long v) { distinctionCount = v; }

    public BigDecimal getPassRate() { return passRate; }
    public void setPassRate(BigDecimal v) { passRate = v; }

    public BigDecimal getFailRate() { return failRate; }
    public void setFailRate(BigDecimal v) { failRate = v; }

    public BigDecimal getWithdrawnRate() { return withdrawnRate; }
    public void setWithdrawnRate(BigDecimal v) { withdrawnRate = v; }

    public BigDecimal getDistinctionRate() { return distinctionRate; }
    public void setDistinctionRate(BigDecimal v) { distinctionRate = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}