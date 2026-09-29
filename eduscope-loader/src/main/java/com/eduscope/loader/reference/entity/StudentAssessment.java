package com.eduscope.loader.reference.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 학생별 평가 제출 결과 Entity.
 *
 * Loader에서는 FK ID를 직접 관리하고,
 * FK 무결성은 Oracle 제약조건으로 보장한다.
 */
@Entity
@Table(
    name = "STUDENT_ASSESSMENT",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_ST_ASSESSMENT",
            columnNames = {
                "STUDENT_COURSE_ID",
                "ASSESSMENT_ID"
            }
        )
    }
)
public class StudentAssessment {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "studentAssessmentSeq"
    )
    @SequenceGenerator(
        name = "studentAssessmentSeq",
        sequenceName = "SEQ_STUDENT_ASSESSMENT_ID",
        allocationSize = 1
    )
    @Column(name = "STUDENT_ASSESSMENT_ID")
    private Long studentAssessmentId;

    @Column(
        name = "STUDENT_COURSE_ID",
        nullable = false
    )
    private Long studentCourseId;

    @Column(
        name = "ASSESSMENT_ID",
        nullable = false
    )
    private Long assessmentId;

    @Column(
        name = "SUBMITTED_DAY",
        nullable = false
    )
    private Integer submittedDay;

    @Column(
        name = "IS_BANKED",
        nullable = false,
        length = 1
    )
    private String isBanked;

    @Column(
        name = "SCORE",
        precision = 7,
        scale = 2
    )
    private BigDecimal score;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public StudentAssessment() {
    }

    public Long getStudentAssessmentId() {
        return studentAssessmentId;
    }

    public Long getStudentCourseId() {
        return studentCourseId;
    }

    public void setStudentCourseId(Long studentCourseId) {
        this.studentCourseId = studentCourseId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Integer getSubmittedDay() {
        return submittedDay;
    }

    public void setSubmittedDay(Integer submittedDay) {
        this.submittedDay = submittedDay;
    }

    public String getIsBanked() {
        return isBanked;
    }

    public void setIsBanked(String isBanked) {
        this.isBanked = isBanked;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}