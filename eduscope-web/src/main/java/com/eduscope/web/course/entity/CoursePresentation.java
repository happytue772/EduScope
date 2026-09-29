package com.eduscope.web.course.entity;

import java.time.LocalDateTime;

import com.eduscope.web.dataset.entity.Dataset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * OULAD 강의 + 개설학기 정보를 나타내는 Entity.
 * Oracle COURSE_PRESENTATION 테이블과 연결된다.
 */
@Entity
@Table(name = "COURSE_PRESENTATION")
public class CoursePresentation {

    @Id
    @Column(name = "COURSE_PRESENTATION_ID", nullable = false)
    private Long coursePresentationId;

    /**
     * COURSE_PRESENTATION.DATASET_ID
     * → DATASET.DATASET_ID FK 연결
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATASET_ID", nullable = false)
    private Dataset dataset;

    @Column(name = "CODE_MODULE", nullable = false, length = 20)
    private String codeModule;

    @Column(name = "CODE_PRESENTATION", nullable = false, length = 20)
    private String codePresentation;

    @Column(name = "MODULE_PRESENTATION_LENGTH", nullable = false)
    private Integer modulePresentationLength;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected CoursePresentation() {
        // JPA 기본 생성자
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public String getCodeModule() {
        return codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public Integer getModulePresentationLength() {
        return modulePresentationLength;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}