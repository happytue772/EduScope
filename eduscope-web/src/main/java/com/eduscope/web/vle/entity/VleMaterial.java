package com.eduscope.web.vle.entity;

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
 * OULAD 온라인 학습자료 기준정보 Entity.
 * Oracle VLE_MATERIAL 테이블과 연결한다.
 */
@Entity
@Table(name = "VLE_MATERIAL")
public class VleMaterial {

    @Id
    @Column(
        name = "VLE_MATERIAL_ID",
        nullable = false
    )
    private Long vleMaterialId;

    /**
     * 어떤 데이터셋에 속하는 자료인지 연결.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "DATASET_ID",
        nullable = false
    )
    private Dataset dataset;

    /**
     * 어떤 강의/개설학기의 자료인지 연결.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "COURSE_PRESENTATION_ID",
        nullable = false
    )
    private CoursePresentation coursePresentation;

    /**
     * OULAD 원본 vle.csv의 id_site.
     */
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

    /**
     * OULAD 원본상 NULL 가능.
     */
    @Column(name = "WEEK_FROM")
    private Integer weekFrom;

    /**
     * OULAD 원본상 NULL 가능.
     */
    @Column(name = "WEEK_TO")
    private Integer weekTo;

    @Column(
        name = "CREATED_AT",
        nullable = false
    )
    private LocalDateTime createdAt;

    protected VleMaterial() {
        // JPA 기본 생성자
    }

    public Long getVleMaterialId() {
        return vleMaterialId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public CoursePresentation getCoursePresentation() {
        return coursePresentation;
    }

    public Long getSourceSiteId() {
        return sourceSiteId;
    }

    public String getActivityType() {
        return activityType;
    }

    public Integer getWeekFrom() {
        return weekFrom;
    }

    public Integer getWeekTo() {
        return weekTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}