package com.eduscope.loader.reference.entity;

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
 * OULAD VLE 학습 자료 기준정보 Entity.
 */
@Entity
@Table(
    name = "VLE_MATERIAL",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_VLE_MATERIAL",
            columnNames = {
                "DATASET_ID",
                "COURSE_PRESENTATION_ID",
                "SOURCE_SITE_ID"
            }
        )
    }
)
public class VleMaterial {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "vleMaterialSeq"
    )
    @SequenceGenerator(
        name = "vleMaterialSeq",
        sequenceName = "SEQ_VLE_MATERIAL_ID",
        allocationSize = 1
    )
    @Column(name = "VLE_MATERIAL_ID")
    private Long vleMaterialId;

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
        name = "SOURCE_SITE_ID",
        nullable = false
    )
    private Long sourceSiteId;

    @Column(
        name = "ACTIVITY_TYPE",
        nullable = false,
        length = 100
    )
    private String activityType;

    @Column(name = "WEEK_FROM")
    private Integer weekFrom;

    @Column(name = "WEEK_TO")
    private Integer weekTo;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    public VleMaterial() {
    }

    public Long getVleMaterialId() {
        return vleMaterialId;
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

    public Long getSourceSiteId() {
        return sourceSiteId;
    }

    public void setSourceSiteId(Long sourceSiteId) {
        this.sourceSiteId = sourceSiteId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Integer getWeekFrom() {
        return weekFrom;
    }

    public void setWeekFrom(Integer weekFrom) {
        this.weekFrom = weekFrom;
    }

    public Integer getWeekTo() {
        return weekTo;
    }

    public void setWeekTo(Integer weekTo) {
        this.weekTo = weekTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}