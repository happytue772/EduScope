package com.eduscope.loader.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "ACTIVITY_RESULT_STAT")
@IdClass(ActivityResultStatId.class)
public class ActivityResultStat {

    @Id
    @Column(name = "JOB_ID")
    private Long jobId;

    @Id
    @Column(name = "COURSE_PRESENTATION_ID")
    private Long coursePresentationId;

    @Id
    @Column(name = "FINAL_RESULT", length = 30)
    private String finalResult;

    @Column(name = "STUDENT_COUNT", nullable = false)
    private Long studentCount;

    @Column(name = "TOTAL_CLICK_COUNT", nullable = false)
    private Long totalClickCount;

    @Column(name = "AVG_CLICK_COUNT",
            nullable = false, precision = 19, scale = 4)
    private BigDecimal avgClickCount;

    @Column(name = "AVG_ACTIVE_DAY_COUNT",
            nullable = false, precision = 19, scale = 4)
    private BigDecimal avgActiveDayCount;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public ActivityResultStat() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long v) { jobId = v; }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public void setCoursePresentationId(Long v) {
        coursePresentationId = v;
    }

    public String getFinalResult() { return finalResult; }
    public void setFinalResult(String v) { finalResult = v; }

    public Long getStudentCount() { return studentCount; }
    public void setStudentCount(Long v) { studentCount = v; }

    public Long getTotalClickCount() { return totalClickCount; }
    public void setTotalClickCount(Long v) { totalClickCount = v; }

    public BigDecimal getAvgClickCount() { return avgClickCount; }
    public void setAvgClickCount(BigDecimal v) { avgClickCount = v; }

    public BigDecimal getAvgActiveDayCount() {
        return avgActiveDayCount;
    }

    public void setAvgActiveDayCount(BigDecimal v) {
        avgActiveDayCount = v;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}