package com.eduscope.web.student.entity;

import java.time.LocalDateTime;

import com.eduscope.web.course.entity.CoursePresentation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 학생과 강의의 수강 관계 Entity.
 */
@Entity
@Table(name = "STUDENT_COURSE")
public class StudentCourse {

    @Id
    @Column(name = "STUDENT_COURSE_ID", nullable = false)
    private Long studentCourseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "COURSE_PRESENTATION_ID",
        nullable = false
    )
    private CoursePresentation coursePresentation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDENT_ID", nullable = false)
    private OuladStudent student;

    @Column(name = "GENDER", length = 10)
    private String gender;

    @Column(name = "REGION", length = 100)
    private String region;

    @Column(name = "HIGHEST_EDUCATION", length = 100)
    private String highestEducation;

    @Column(name = "IMD_BAND", length = 30)
    private String imdBand;

    @Column(name = "AGE_BAND", length = 30)
    private String ageBand;

    @Column(name = "NUM_OF_PREV_ATTEMPTS")
    private Integer numOfPrevAttempts;

    @Column(name = "STUDIED_CREDITS")
    private Integer studiedCredits;

    @Column(name = "DISABILITY", length = 1)
    private String disability;

    @Column(name = "FINAL_RESULT", nullable = false, length = 30)
    private String finalResult;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected StudentCourse() {
        // JPA 기본 생성자
    }

    public Long getStudentCourseId() {
        return studentCourseId;
    }

    public CoursePresentation getCoursePresentation() {
        return coursePresentation;
    }

    public OuladStudent getStudent() {
        return student;
    }

    public String getGender() {
        return gender;
    }

    public String getRegion() {
        return region;
    }

    public String getHighestEducation() {
        return highestEducation;
    }

    public String getImdBand() {
        return imdBand;
    }

    public String getAgeBand() {
        return ageBand;
    }

    public Integer getNumOfPrevAttempts() {
        return numOfPrevAttempts;
    }

    public Integer getStudiedCredits() {
        return studiedCredits;
    }

    public String getDisability() {
        return disability;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}