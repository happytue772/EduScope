package com.eduscope.web.student.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Id;



/**
 * 학생의 강의 등록/수강취소 정보를 나타내는 Entity.
 *
 * PK = STUDENT_COURSE_ID
 * 동시에 STUDENT_COURSE의 FK 역할도 한다.
 */
@Entity
@Table(name = "STUDENT_REGISTRATION")
public class StudentRegistration {

    @Id
    @Column(name = "STUDENT_COURSE_ID", nullable = false)
    private Long studentCourseId;

    /**
     * STUDENT_COURSE_ID를 이용해
     * STUDENT_COURSE Entity를 조회하기 위한 읽기 전용 관계.
     *
     * Loader에서 사용했던 직접 FK ID 구조를 유지한다.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "STUDENT_COURSE_ID",
        insertable = false,
        updatable = false
    )
    private StudentCourse studentCourse;

    @Column(name = "REGISTRATION_DAY")
    private Integer registrationDay;

    @Column(name = "UNREGISTRATION_DAY")
    private Integer unregistrationDay;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected StudentRegistration() {
        // JPA 기본 생성자
    }

    public Long getStudentCourseId() {
        return studentCourseId;
    }

    public StudentCourse getStudentCourse() {
        return studentCourse;
    }

    public Integer getRegistrationDay() {
        return registrationDay;
    }

    public Integer getUnregistrationDay() {
        return unregistrationDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}