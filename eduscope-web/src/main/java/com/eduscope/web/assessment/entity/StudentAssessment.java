package com.eduscope.web.assessment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eduscope.web.student.entity.StudentCourse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 학생별 평가 제출 결과 Entity.
 * Oracle STUDENT_ASSESSMENT 테이블과 연결된다.
 */
@Entity
@Table(name = "STUDENT_ASSESSMENT")
public class StudentAssessment {

    @Id
    @Column(
        name = "STUDENT_ASSESSMENT_ID",
        nullable = false
    )
    private Long studentAssessmentId;

    /**
     * 어떤 학생의 어떤 강의 수강 건인지 연결.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "STUDENT_COURSE_ID",
        nullable = false
    )
    private StudentCourse studentCourse;

    /**
     * 어떤 과제/시험인지 연결.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ASSESSMENT_ID",
        nullable = false
    )
    private Assessment assessment;

    /**
     * 강의 시작일을 0으로 한 상대 제출일.
     */
    @Column(
        name = "SUBMITTED_DAY",
        nullable = false
    )
    private Integer submittedDay;

    /**
     * OULAD 원본 is_banked를
     * Loader에서 Y/N으로 변환해 저장한 값.
     */
    @Column(
        name = "IS_BANKED",
        nullable = false,
        length = 1
    )
    private String isBanked;

    /**
     * 평가 점수.
     * 원본상 NULL 가능.
     */
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

    protected StudentAssessment() {
        // JPA 기본 생성자
    }

    public Long getStudentAssessmentId() {
        return studentAssessmentId;
    }

    public StudentCourse getStudentCourse() {
        return studentCourse;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public Integer getSubmittedDay() {
        return submittedDay;
    }

    public String getIsBanked() {
        return isBanked;
    }

    public BigDecimal getScore() {
        return score;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}