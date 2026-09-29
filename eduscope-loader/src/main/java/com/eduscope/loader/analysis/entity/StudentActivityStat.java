package com.eduscope.loader.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 학생별 VLE 활동 MapReduce 결과.
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

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public StudentActivityStat() {
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getStudentCourseId() {
        return studentCourseId;
    }

    public void setStudentCourseId(Long studentCourseId) {
        this.studentCourseId = studentCourseId;
    }

    public Long getTotalClickCount() {
        return totalClickCount;
    }

    public void setTotalClickCount(Long totalClickCount) {
        this.totalClickCount = totalClickCount;
    }

    public Long getActiveDayCount() {
        return activeDayCount;
    }

    public void setActiveDayCount(Long activeDayCount) {
        this.activeDayCount = activeDayCount;
    }

    public Long getUsedMaterialCount() {
        return usedMaterialCount;
    }

    public void setUsedMaterialCount(Long usedMaterialCount) {
        this.usedMaterialCount = usedMaterialCount;
    }

    public BigDecimal getAvgDailyClickCount() {
        return avgDailyClickCount;
    }

    public void setAvgDailyClickCount(
            BigDecimal avgDailyClickCount) {
        this.avgDailyClickCount = avgDailyClickCount;
    }

    public Integer getFirstActivityDay() {
        return firstActivityDay;
    }

    public void setFirstActivityDay(Integer firstActivityDay) {
        this.firstActivityDay = firstActivityDay;
    }

    public Integer getLastActivityDay() {
        return lastActivityDay;
    }

    public void setLastActivityDay(Integer lastActivityDay) {
        this.lastActivityDay = lastActivityDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}