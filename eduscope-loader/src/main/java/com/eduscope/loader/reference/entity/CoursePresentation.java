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
 * OULAD의 강의 + 개설 차수를 관리한다.
 */
@Entity
@Table(
    name = "COURSE_PRESENTATION",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_COURSE_PRESENT",
            columnNames = {
                "DATASET_ID",
                "CODE_MODULE",
                "CODE_PRESENTATION"
            }
        )
    }
)
public class CoursePresentation {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "course_presentation_seq"
    )
    @SequenceGenerator(
        name = "course_presentation_seq",
        sequenceName = "SEQ_COURSE_PRESENTATION_ID",
        allocationSize = 1
    )
    @Column(name = "COURSE_PRESENTATION_ID")
    private Long coursePresentationId;

    public Long getCoursePresentationId() {
		return coursePresentationId;
	}

	public void setCoursePresentationId(Long coursePresentationId) {
		this.coursePresentationId = coursePresentationId;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public String getCodeModule() {
		return codeModule;
	}

	public void setCodeModule(String codeModule) {
		this.codeModule = codeModule;
	}

	public String getCodePresentation() {
		return codePresentation;
	}

	public void setCodePresentation(String codePresentation) {
		this.codePresentation = codePresentation;
	}

	public Integer getModulePresentationLength() {
		return modulePresentationLength;
	}

	public void setModulePresentationLength(Integer modulePresentationLength) {
		this.modulePresentationLength = modulePresentationLength;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
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

    public CoursePresentation() {
    }

    // Source → Generate Getters and Setters
}