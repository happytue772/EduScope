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
 * OULAD 원본 학생 ID를 내부 학생 PK와 연결한다.
 */
@Entity
@Table(
    name = "OULAD_STUDENT",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_OULAD_STUDENT",
            columnNames = {"DATASET_ID", "SOURCE_STUDENT_ID"}
        )
    }
)
public class OuladStudent {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "oulad_student_seq"
    )
    @SequenceGenerator(
        name = "oulad_student_seq",
        sequenceName = "SEQ_OULAD_STUDENT_ID",
        allocationSize = 1
    )
    @Column(name = "STUDENT_ID")
    private Long studentId;

    public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Long getSourceStudentId() {
		return sourceStudentId;
	}

	public void setSourceStudentId(Long sourceStudentId) {
		this.sourceStudentId = sourceStudentId;
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

    @Column(name = "SOURCE_STUDENT_ID", nullable = false)
    private Long sourceStudentId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public OuladStudent() {
    }

    // Source → Generate Getters and Setters
}