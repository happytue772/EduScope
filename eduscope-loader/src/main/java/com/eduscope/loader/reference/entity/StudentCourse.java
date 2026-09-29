package com.eduscope.loader.reference.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 학생과 특정 강의 개설차수의 참여 관계 및
 * studentInfo.csv의 학습자 특성을 관리한다.
 */
@Entity
@Table(
    name = "STUDENT_COURSE",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_STUDENT_COURSE",
            columnNames = {
                "COURSE_PRESENTATION_ID",
                "STUDENT_ID"
            }
        )
    }
)
public class StudentCourse {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "student_course_seq"
    )
    @SequenceGenerator(
        name = "student_course_seq",
        sequenceName = "SEQ_STUDENT_COURSE_ID",
        allocationSize = 1
    )
    @Column(name = "STUDENT_COURSE_ID")
    private Long studentCourseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "COURSE_PRESENTATION_ID",
        nullable = false
    )
    private CoursePresentation coursePresentation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "STUDENT_ID",
        nullable = false
    )
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

    public Long getStudentCourseId() {
		return studentCourseId;
	}

	public void setStudentCourseId(Long studentCourseId) {
		this.studentCourseId = studentCourseId;
	}

	public CoursePresentation getCoursePresentation() {
		return coursePresentation;
	}

	public void setCoursePresentation(CoursePresentation coursePresentation) {
		this.coursePresentation = coursePresentation;
	}

	public OuladStudent getStudent() {
		return student;
	}

	public void setStudent(OuladStudent student) {
		this.student = student;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getHighestEducation() {
		return highestEducation;
	}

	public void setHighestEducation(String highestEducation) {
		this.highestEducation = highestEducation;
	}

	public String getImdBand() {
		return imdBand;
	}

	public void setImdBand(String imdBand) {
		this.imdBand = imdBand;
	}

	public String getAgeBand() {
		return ageBand;
	}

	public void setAgeBand(String ageBand) {
		this.ageBand = ageBand;
	}

	public Integer getNumOfPrevAttempts() {
		return numOfPrevAttempts;
	}

	public void setNumOfPrevAttempts(Integer numOfPrevAttempts) {
		this.numOfPrevAttempts = numOfPrevAttempts;
	}

	public Integer getStudiedCredits() {
		return studiedCredits;
	}

	public void setStudiedCredits(Integer studiedCredits) {
		this.studiedCredits = studiedCredits;
	}

	public String getDisability() {
		return disability;
	}

	public void setDisability(String disability) {
		this.disability = disability;
	}

	public String getFinalResult() {
		return finalResult;
	}

	public void setFinalResult(String finalResult) {
		this.finalResult = finalResult;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

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

    public StudentCourse() {
    }

    // Source → Generate Getters and Setters
}