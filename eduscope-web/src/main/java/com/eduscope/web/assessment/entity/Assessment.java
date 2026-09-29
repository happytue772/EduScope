package com.eduscope.web.assessment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eduscope.web.course.entity.CoursePresentation;
import com.eduscope.web.dataset.entity.Dataset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * OULAD 평가/과제 기준정보 Entity.
 * Oracle ASSESSMENT 테이블과 연결된다.
 */
@Entity
@Table(name = "ASSESSMENT")
public class Assessment {

    @Id
    @Column(name = "ASSESSMENT_ID", nullable = false)
    private Long assessmentId;

    /**
     * ASSESSMENT.DATASET_ID
     * → DATASET.DATASET_ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATASET_ID", nullable = false)
    private Dataset dataset;

    /**
     * ASSESSMENT.COURSE_PRESENTATION_ID
     * → COURSE_PRESENTATION.COURSE_PRESENTATION_ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "COURSE_PRESENTATION_ID",
        nullable = false
    )
    private CoursePresentation coursePresentation;

    /**
     * OULAD 원본 id_assessment.
     */
    @Column(name = "SOURCE_ASSESSMENT_ID", nullable = false)
    private Long sourceAssessmentId;

    /**
     * TMA / CMA / Exam
     */
    @Column(name = "ASSESSMENT_TYPE", nullable = false, length = 20)
    private String assessmentType;

    /**
     * 강의 시작일을 0으로 한 상대 제출기한.
     * NULL 가능.
     */
    @Column(name = "ASSESSMENT_DUE_DAY")
    private Integer assessmentDueDay;

    @Column(
        name = "ASSESSMENT_WEIGHT",
        nullable = false,
        precision = 7,
        scale = 2
    )
    private BigDecimal assessmentWeight;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected Assessment() {
        // JPA 기본 생성자
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public CoursePresentation getCoursePresentation() {
        return coursePresentation;
    }

    public Long getSourceAssessmentId() {
        return sourceAssessmentId;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public Integer getAssessmentDueDay() {
        return assessmentDueDay;
    }

    public BigDecimal getAssessmentWeight() {
        return assessmentWeight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}