package com.eduscope.loader.reference.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eduscope.loader.dataset.entity.Dataset;

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
 * OULAD 평가/시험 기준정보.
 */
@Entity
@Table(
    name = "ASSESSMENT",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_ASSESS_SOURCE",
            columnNames = {
                "DATASET_ID",
                "SOURCE_ASSESSMENT_ID"
            }
        )
    }
)
public class Assessment {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "assessmentSeq"
    )
    @SequenceGenerator(
        name = "assessmentSeq",
        sequenceName = "SEQ_ASSESSMENT_ID",
        allocationSize = 1
    )
    @Column(name = "ASSESSMENT_ID")
    private Long assessmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "DATASET_ID",
        nullable = false
    )
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "COURSE_PRESENTATION_ID",
        nullable = false
    )
    private CoursePresentation coursePresentation;

    @Column(
        name = "SOURCE_ASSESSMENT_ID",
        nullable = false
    )
    private Long sourceAssessmentId;

    @Column(
        name = "ASSESSMENT_TYPE",
        nullable = false,
        length = 20
    )
    private String assessmentType;

    @Column(name = "ASSESSMENT_DUE_DAY")
    private Integer assessmentDueDay;

    @Column(
        name = "ASSESSMENT_WEIGHT",
        nullable = false,
        precision = 7,
        scale = 2
    )
    private BigDecimal assessmentWeight;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public Assessment() {
        // JPA 기본 생성자
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public CoursePresentation getCoursePresentation() {
        return coursePresentation;
    }

    public void setCoursePresentation(
            CoursePresentation coursePresentation) {
        this.coursePresentation = coursePresentation;
    }

    public Long getSourceAssessmentId() {
        return sourceAssessmentId;
    }

    public void setSourceAssessmentId(
            Long sourceAssessmentId) {
        this.sourceAssessmentId = sourceAssessmentId;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(
            String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public Integer getAssessmentDueDay() {
        return assessmentDueDay;
    }

    public void setAssessmentDueDay(
            Integer assessmentDueDay) {
        this.assessmentDueDay = assessmentDueDay;
    }

    public BigDecimal getAssessmentWeight() {
        return assessmentWeight;
    }

    public void setAssessmentWeight(
            BigDecimal assessmentWeight) {
        this.assessmentWeight = assessmentWeight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}