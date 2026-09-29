package com.eduscope.loader.reference.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 학생의 강의 등록/등록해제 정보를 저장한다.
 *
 * STUDENT_COURSE_ID는
 * STUDENT_REGISTRATION의 PK이면서
 * DB에서 STUDENT_COURSE를 참조하는 FK이다.
 */
@Entity
@Table(name = "STUDENT_REGISTRATION")
public class StudentRegistration {

    /**
     * 별도 Sequence를 사용하지 않는다.
     * 기존 STUDENT_COURSE의 PK를 그대로 사용한다.
     */
    @Id
    @Column(name = "STUDENT_COURSE_ID", nullable = false)
    private Long studentCourseId;

    @Column(name = "REGISTRATION_DAY")
    private Integer registrationDay;

    @Column(name = "UNREGISTRATION_DAY")
    private Integer unregistrationDay;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public StudentRegistration() {
        // JPA 기본 생성자
    }

    public Long getStudentCourseId() {
        return studentCourseId;
    }

    public void setStudentCourseId(Long studentCourseId) {
        this.studentCourseId = studentCourseId;
    }

    public Integer getRegistrationDay() {
        return registrationDay;
    }

    public void setRegistrationDay(Integer registrationDay) {
        this.registrationDay = registrationDay;
    }

    public Integer getUnregistrationDay() {
        return unregistrationDay;
    }

    public void setUnregistrationDay(Integer unregistrationDay) {
        this.unregistrationDay = unregistrationDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}